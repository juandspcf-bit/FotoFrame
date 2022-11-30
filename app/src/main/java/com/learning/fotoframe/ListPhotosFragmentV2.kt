package com.learning.fotoframe


import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.learning.fotoframe.adapters.recyclerView.RecyclerViewAdapter
import com.learning.fotoframe.adapters.recyclerView.RecyclerViewBuilder
import com.learning.fotoframe.adapters.recyclerView.ViewsInRowHolder
import com.learning.fotoframe.adapters.recyclerView.viewHolders.summaryBP.ViewsInMyLinkRowHolder
import com.learning.fotoframe.databinding.FragmentListPhotosV2Binding
import com.learning.fotoframe.viewmodels.AppMainViewModel
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.stream.Collectors


class ListPhotosFragmentV2 : Fragment() {
    private lateinit var binding: FragmentListPhotosV2Binding
    private var controller: NavController? = null
    private lateinit var db: FirebaseFirestore
    private lateinit var storageReference: StorageReference
    private var appMainViewModel: AppMainViewModel? = null
    private var fireBaseUtilsApp: FirebaseUtilsApp = FirebaseUtilsApp()
    private lateinit var materialToolbar:MaterialToolbar

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
                    Log.d(TAG, "name an size of the file: $stringName : $stringSize")
                    if (stringSize.toInt() >= 500000) {
                        context?.let {
                            showMaterialDialogue(
                                "The image is to big",
                                "Image must be less than 500kb", it
                            )
                        }
                        return@use
                    }
                    Log.d(TAG, "uri: extractThumbnail ${uri.toString()}")
                    showBottomDialog(uri)
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
        val vista = LayoutInflater.from(context).inflate(R.layout.selection_set_botton_dialog, null)
        val imageView = vista.findViewById<ImageView>(R.id.selectedImgImageView)
        imageView.setImageURI(uri)
        val group = vista.findViewById<View>(R.id.group2)
        val createNewSetButton = vista.findViewById<Button>(R.id.createNewSetButton)
        val addToSetButton = vista.findViewById<Button>(R.id.addToSetButton)

        val textInputLayout = vista.findViewById<TextInputLayout>(R.id.outlinedTextField)

        val viewById = vista.findViewById<TextInputLayout>(R.id.selectionMenu)
        val editTextBasic = viewById.editText
        val editText = editTextBasic as MaterialAutoCompleteTextView?

        appMainViewModel?.mutableLiveDataSets?.observe(viewLifecycleOwner, Observer { sets ->
            if (sets == null || sets.size == 0) return@Observer
            val setsArrays = sets.toTypedArray()
            editText?.setSimpleItems(setsArrays)
        })

        addToSetButton.setOnClickListener {
            if (textInputLayout.editText != null && textInputLayout.editText!!.text.toString()
                    .isNotEmpty()
            ) {
                addToSetButton.isEnabled = false
                val set = textInputLayout.editText!!.text.toString()
                val data: MutableMap<String, Any> = HashMap()
                data["name"] = set
                appMainViewModel!!.mutableLiveDataSets.value!!.add(set)

                fireBaseUtilsApp.addSetToDataBase(data){ status->
                    if (status=="success"){
                        fireBaseUtilsApp.addToFireStorage(
                            uri,
                            set,
                            context,
                            requireActivity()
                        ) { aBoolean: Boolean ->
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
                }

            } else if ((editText != null) && (editText.text != null) && editText.text.toString()
                    .isNotEmpty()
            ) {
                addToSetButton.isEnabled = false
                val set = editText.text.toString()


                fireBaseUtilsApp.addToFireStorage(
                    uri,
                    set,
                    context,
                    requireActivity()
                ) { aBoolean: Boolean ->
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
        }
        createNewSetButton.setOnClickListener { group.visibility = View.VISIBLE }
        dialog.setCancelable(true)
        dialog.setContentView(vista)
        dialog.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        controller = NavHostFragment.findNavController(this)
        val windowInsetsController2 = WindowCompat.getInsetsController(
            requireActivity().window, requireActivity().window.decorView
        )

        requireActivity().window.statusBarColor =
            resources.getColor(R.color.status_bar_color_for_list_fragment, null)

        appMainViewModel = ViewModelProvider(requireActivity())[AppMainViewModel::class.java]

        FirebaseApp.initializeApp(requireContext())
        db = FirebaseFirestore.getInstance()

        fireBaseUtilsApp.getSets { sets ->
            appMainViewModel?.setMutableLiveDataSets(sets)
        }

        storageReference = FirebaseStorage.getInstance().reference

        setupPermissionsRead()
        setupPermissionsWrite()



    }





    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_list_photos_v2,
            container,
            false
        )
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.settings ->{
                    controller?.navigate(R.id.action_listPhotosFragmentV2_to_settingsFragment)
                }
            }
            false
        }
        binding.floatingActionButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            startOpenForResult.launch(intent)
        }

    }

    override fun onResume() {
        super.onResume()
        fireBaseUtilsApp.getSets { strings ->
            val map = strings.map { set -> SetWithBoolean(set, isVisible) }
            showRecyclerView(map)
        }


    }

    private fun showRecyclerView(list: List<SetWithBoolean>) {

        val recyclerViewBuilder = RecyclerViewBuilder(
            list.size,
            context,
            binding.recyclerView,
            R.layout.row_set_layout,
            { id: Int, position: Int ->
                val set = list[position].set
                val action =
                    ListPhotosFragmentV2Directions.actionListPhotosFragmentV2ToPhotoSetListFragment()
                action.set = set ?: "0"
                Log.d(TAG, "showRecyclerView: ${action.set}")
                controller?.navigate(action)


            },
            { position: Int, holder: ViewsInRowHolder?, adapter: RecyclerViewAdapter ->
                val viewsInMyLinkRowHolder = holder as ViewsInMyLinkRowHolder
                viewsInMyLinkRowHolder.rowSetNameTextView.text = list[position].set
                val collectionReferenceLinks = db.collection("links");
                collectionReferenceLinks.whereEqualTo("set", list[position].set).limit(1)
                    .addSnapshotListener { snapShoot, _ ->
                        if (snapShoot?.isEmpty == true) return@addSnapshotListener
                        val link =
                            snapShoot?.documents?.get(0)?.data?.get("linkThumbnail").toString()
                        Picasso.get()
                            .load(link)
                            .placeholder(R.drawable.download)
                            .error(R.drawable.close)
                            .into(viewsInMyLinkRowHolder.rowSetImageView)
                    }


                viewsInMyLinkRowHolder.rowSetDeleteButton.setOnClickListener {
                    fireBaseUtilsApp.getAllElements(list[position].set){myLinks ->
                        Log.d(TAG, "showRecyclerView: $myLinks")
                        val elementsToDelete = myLinks.map { myLink -> myLink.storageName }

                        val coroutineScope = CoroutineScope(Dispatchers.Main.immediate)
                        coroutineScope.launch {
                            backGroundCoroutine(elementsToDelete, list, position)
                        }





                    }
                    
                }

                viewsInMyLinkRowHolder.rowPlayButton.setOnClickListener {

                    fireBaseUtilsApp.getAllElements(list[position].set){
                        appMainViewModel?.setMutableLiveDataMyLink(it)
                    }

                    controller?.navigate(R.id.action_listPhotosFragmentV2_to_showSlidesFragmentV22)

                }

            },
            ViewsInMyLinkRowHolder()
        )

        recyclerViewBuilder.buildWithLinearLayoutManager()


    }

    private suspend fun backGroundCoroutine(elementsToDelete: List<String>,
                                            list: List<SetWithBoolean>,
                                            position: Int){

        withContext(Dispatchers.IO){
            deleteSet(elementsToDelete, list, position)
        }

    }

    private fun deleteSet(
        elementsToDelete: List<String>,
        list: List<SetWithBoolean>,
        position: Int
    ) {

        Log.d(TAG, "deleteSet: call two times")
        for (element in elementsToDelete) {
            Log.d(TAG, "deleteSet: $element")
            storageReference
                .child("memories")
                .child(element).delete().addOnSuccessListener {
                    db.collection("links").document(element)
                        .delete()
                        .addOnSuccessListener {
                            storageReference
                                .child("memoriesThumbnail")
                                .child(element).delete().addOnSuccessListener {
                                }.addOnFailureListener {
                                    Log.d("Delete", "memoriesThumbnail: error")
                                }

                        }
                        .addOnFailureListener {
                            Log.d("Delete", "memories links: error")
                        };
                }.addOnFailureListener {
                    Log.d("Delete", "memories : error")
                }
        }


        db.collection("sets")
            .whereEqualTo("name", list[position].set).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val id = document.id
                    Log.d(TAG, "showRecyclerView id: $id")
                    db.collection("sets").document(id).delete()
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }





    }


    private fun setupPermissionsWrite() {
        val permission = context?.let {
            Log.i("Permission", "not null")
            ContextCompat.checkSelfPermission(
                it,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i("Permission", "Permission denied")
            makeRequestWrite()
        }
    }

    private fun setupPermissionsRead() {
        val permission = context?.let {
            Log.i("Permission", "not null")
            ContextCompat.checkSelfPermission(
                it,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i("Permission", "Permission denied")
            makeRequestWrite()
        }
    }

    private val RECORD_REQUEST_CODE = 101
    private fun makeRequestWrite() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            RECORD_REQUEST_CODE
        )
    }

    private fun makeRequestRead() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            RECORD_REQUEST_CODE
        )
    }


    data class MyLink(
        var set: String,
        var link: String,
        var storageName: String = "",
        var linkThumbnail: String = ""
    )

    data class SetWithBoolean(
        var set: String,
        var isVisible: Boolean
    )

    companion object {
        private val TAG = ListPhotosFragmentV2::class.java.simpleName
    }
}
