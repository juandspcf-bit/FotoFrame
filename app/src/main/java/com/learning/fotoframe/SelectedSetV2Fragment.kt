package com.learning.fotoframe

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.firebase.firestore.FirebaseFirestore
import com.learning.fotoframe.databinding.FragmentSelectedSetV2Binding

import com.learning.fotoframe.viewmodels.AppMainViewModel

class SelectedSetV2Fragment : Fragment() {
    private lateinit var binding: FragmentSelectedSetV2Binding
    private lateinit var controller: NavController
    private lateinit var db: FirebaseFirestore
    private val myLinkList: MutableList<ListPhotosFragmentV2.MyLink> = ArrayList()
    private lateinit var appMainViewModel: AppMainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = FirebaseFirestore.getInstance()
        controller = NavHostFragment.findNavController(this)
        appMainViewModel = ViewModelProvider(requireActivity())[AppMainViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_selected_set_v2,
            container,
            false
        )
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val editText = binding.menu.editText as MaterialAutoCompleteTextView


        appMainViewModel.mutableLiveDataSets.observe(viewLifecycleOwner, Observer { sets ->
            sets?.let {
                val setsArray = it.toTypedArray()
                editText.setSimpleItems(setsArray)
            }
        })
        binding.button.setOnClickListener { //if(SelectSetFragment.this.position==-1) return;


            FirebaseUtilsApp().getAllElements(editText.text.toString()){
                myLinkList.clear()
                myLinkList.addAll(0,it)
                appMainViewModel.setMutableLiveDataMyLink(myLinkList)
            }

            controller.navigate(R.id.action_selectedSetV2Fragment_to_showSlidesFragmentV2)
        }
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requireActivity().window.insetsController?.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        } else {
            val decorView = requireActivity().window.decorView
            val flags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_VISIBLE)
            decorView.systemUiVisibility = flags
        }
    }


    companion object {
        private val TAG = SelectedSetV2Fragment::class.java.simpleName

    }
}