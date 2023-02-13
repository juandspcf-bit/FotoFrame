package com.learning.fotoframe

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.learning.fotoframe.adapters.recyclerView.RecyclerViewAdapter
import com.learning.fotoframe.adapters.recyclerView.RecyclerViewBuilder
import com.learning.fotoframe.adapters.recyclerView.ViewsInRowHolder
import com.learning.fotoframe.adapters.recyclerView.viewHolders.summaryBP.ViewsInMyLinks2RowHolder
import com.learning.fotoframe.databinding.FragmentPhotoSetListBinding
import com.learning.fotoframe.viewmodels.AppMainViewModel
import com.squareup.picasso.Picasso


class PhotoSetListFragment : Fragment() {
    private lateinit var binding: FragmentPhotoSetListBinding
    private lateinit var controller: NavController
    private lateinit var db: FirebaseFirestore
    private lateinit var storageReference: StorageReference
    private var appMainViewModel: AppMainViewModel? = null
    private lateinit var materialToolbar:MaterialToolbar
    private lateinit var visibleList: MutableList<MyLink2>
    private lateinit var elementsToDelete: List<MyLink2>
    private var targetSize: Int = 0
    private var isDeleting:Boolean = false
    private lateinit var set:String
    private lateinit var auth: FirebaseAuth


    private var startOpenForResult = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            if (data != null) {
                val uri = data.data
                uri?.let {
                    context?.contentResolver?.query(it, null, null, null, null)
                }?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    cursor.moveToFirst()
                    val stringName = cursor.getString(nameIndex)
                    val stringSize = cursor.getString(sizeIndex)

                    if (stringSize.toInt() >= 500000) {
                        context?.let {
                            showMaterialDialogue(
                                "The image is to big",
                                "Image must be less than 500kb", it
                            )
                        }
                        return@use
                    }

                    showBottomDialog(uri)
                }

            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        controller = NavHostFragment.findNavController(this)
        appMainViewModel = ViewModelProvider(requireActivity())[AppMainViewModel::class.java]
        FirebaseApp.initializeApp(requireContext())
        db = FirebaseFirestore.getInstance()
        storageReference = FirebaseStorage.getInstance().reference
        auth = Firebase.auth
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_photo_set_list,
            container,
            false
        )
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val user = auth.currentUser
        if (user==null){
            Toast.makeText(context, "Sign Up before", Toast.LENGTH_SHORT).show()
            return
        }
        val userEmail = user.email

        appMainViewModel?.mutableLiveDataMyLink2?.observe(viewLifecycleOwner) { myList ->
            visibleList = myList
            if(targetSize==visibleList.size){
                isDeleting = false
                Log.d("Delete", "onViewCreated: deleting finished")
            }
            showRecyclerView(visibleList)
        }

        binding.photoListTopAppBar.setNavigationOnClickListener {
            controller.popBackStack()
        }

        binding.photoListTopAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.deleteItems -> {
                    if(isDeleting) {
                        Log.d("Delete", "onViewCreated: is deleting")
                        return@setOnMenuItemClickListener false
                    }

                    elementsToDelete = visibleList
                        .filter { myLink2 -> myLink2.isVisible }

                    Log.d("Delete", "onViewCreated: $elementsToDelete")

                    targetSize = visibleList.size-elementsToDelete.size

                    if(elementsToDelete.isNotEmpty()){
                        isDeleting = true
                        for (myLink2 in elementsToDelete){
                            storageReference
                                .child("$userEmail-memories")
                                .child(myLink2.storageName).delete().addOnSuccessListener {
                                    Log.d("Delete", "onViewCreated: success")
                                    db.collection("$userEmail-links").document(myLink2.storageName)
                                        .delete()
                                        .addOnSuccessListener {
                                            val filter = visibleList
                                                .filter { myLink2 -> myLink2.isVisible.not() }
                                            appMainViewModel?.mutableLiveDataMyLink2?.value = filter.toMutableList()
                                            Log.d("Delete", "onViewCreated: success")
                                        }
                                        .addOnFailureListener{
                                            Log.d("Delete", "onViewCreated: error")
                                        };
                                }.addOnFailureListener {
                                    Log.d("Delete", "onViewCreated: error")
                                }

                        }

                        for (myLink2 in elementsToDelete){
                            storageReference
                                .child("memoriesThumbnail")
                                .child(myLink2.storageName).delete().addOnSuccessListener {
                                    Log.d("Delete", "onViewCreated: success")
                                    db.collection("links").document(myLink2.storageName)
                                        .delete()
                                        .addOnSuccessListener {
                                        }
                                        .addOnFailureListener{
                                            Log.d("Delete", "onViewCreated: error")
                                        };
                                }.addOnFailureListener {
                                    Log.d("Delete", "onViewCreated: error")
                                }
                        }

                    }

                    true
                }

                R.id.addItems -> {
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    intent.type = "image/*"
                    startOpenForResult.launch(intent)

                    true
                }

                else -> false
            }
        }

    }

    private fun showRecyclerView(list: List<*>) {

        val recyclerViewBuilder = RecyclerViewBuilder(
            list.size,
            context,
            binding.photoListRecyclerView,
            R.layout.row_photo_set_list_layout,
            { id: Int, position: Int ->


            },
            { position: Int, holder: ViewsInRowHolder?, adapter: RecyclerViewAdapter ->
                val viewsInMyLink2RowHolder = holder as ViewsInMyLinks2RowHolder
                val myLink2 = list[position] as MyLink2

                if(myLink2.isVisible){
                    viewsInMyLink2RowHolder.deletePhotoRowButton.visibility = View.VISIBLE
                }else {
                    viewsInMyLink2RowHolder.deletePhotoRowButton.visibility = View.INVISIBLE
                }

                viewsInMyLink2RowHolder.photoRowImageView.setOnClickListener {
                    if(myLink2.isVisible.not()){
                        val myUpdatedLink = list[position] as MyLink2
                        myUpdatedLink.isVisible = true

                    }else {
                        val myUpdatedLink = list[position] as MyLink2
                        myUpdatedLink.isVisible = false

                    }

                    adapter.notifyItemChanged(position)
                }

                Picasso.get()
                    .load(myLink2.linkThumbnail)
                    .placeholder(R.drawable.download)
                    .error(R.drawable.close)
                    .into(viewsInMyLink2RowHolder.photoRowImageView)

            },
            ViewsInMyLinks2RowHolder()
        )

        recyclerViewBuilder.buildWithGridLayoutManager()


    }

    override fun onStart() {
        super.onStart()
        arguments?.let {
            val args = PhotoSetListFragmentArgs.fromBundle(it)
            Log.d("SET", "onStart: ${args.set}")
            binding.photoListTopAppBar.title=args.set
            set = args.set

            val user = auth.currentUser
            if (user==null){
                Toast.makeText(context, "Sign Up before", Toast.LENGTH_SHORT)
                return
            }
            val userEmail = user.email

            Log.d("pathString", "storeImageInDB: $userEmail-memories")

            val collectionReferenceLinks = db.collection("$userEmail-links");
            collectionReferenceLinks.whereEqualTo("set", args.set).addSnapshotListener { query, e ->
                val documents = query?.documents
                val myLinks2 = documents?.map { document ->
                    val set = document.data?.get("set").toString()
                    val link = document.data?.get("link").toString()
                    val storageName = document.data?.get("storageName").toString()
                    val linkThumbnail = document.data?.get("linkThumbnail").toString()
                    MyLink2(set=set, link=link, storageName=storageName, linkThumbnail=linkThumbnail)
                    //
                }

                if (myLinks2 != null) {
                    appMainViewModel?.setMutableLiveDataMyLink2(myLinks2.toMutableList())
                }

            }

        }
    }


    private fun showMaterialDialogue(title: String, message: String, context: Context) {
        MaterialAlertDialogBuilder(context)

            .setTitle(title)
            .setIcon(R.drawable.ic_baseline_error_outline_24)

            .setMessage(message)
            .setPositiveButton("Accept") { dialog, which ->
                // Respond to positive button press
            }
            .show()

    }

    private fun showBottomDialog(uri: Uri?) {
        val dialog = BottomSheetDialog(requireContext())
        val vista = LayoutInflater.from(context).inflate(R.layout.selection_botton_dialog, null)
        val imageView = vista.findViewById<ImageView>(R.id.selectedImgImageView2)
        imageView.setImageURI(uri)
        val addToSetButton = vista.findViewById<Button>(R.id.addToSetButton2)

        val firebaseUtilsApp = FirebaseUtilsApp()
        addToSetButton.setOnClickListener{

            val user = auth.currentUser
            if (user==null){
                Toast.makeText(context, "Sign Up before", Toast.LENGTH_SHORT)
                return@setOnClickListener
            }
            val userEmail = user.email

            Log.d("pathString", "storeImageInDB: $userEmail-memories")
            firebaseUtilsApp.addToFireStorage(
                uri,
                set,
                context,
                requireActivity(),
                userEmail
            ){ aBoolean: Boolean ->
                if (aBoolean) {
                    dialog.dismiss()
                    Toast.makeText(
                        context,
                        "Photo successfully added",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        context,
                        "Error in saving photo",
                        Toast.LENGTH_LONG
                    ).show()
                    addToSetButton.isEnabled = true
                }
            }
        }

        dialog.setCancelable(true)
        dialog.setContentView(vista)
        dialog.show()



    }

    data class MyLink2(
        var set: String,
        var link: String,
        var storageName: String = "",
        var isVisible: Boolean = false,
        var linkThumbnail: String = ""
    )

}