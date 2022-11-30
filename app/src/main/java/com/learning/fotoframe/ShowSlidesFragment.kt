package com.learning.fotoframe

import android.os.Build

import com.google.firebase.storage.StorageReference
import com.smarteist.autoimageslider.SliderView
import com.learning.fotoframe.viewmodels.AppMainViewModel
import android.os.Bundle
import android.view.*
import com.google.firebase.FirebaseApp
import com.google.firebase.storage.FirebaseStorage
import androidx.lifecycle.ViewModelProvider
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.learning.fotoframe.R
import com.learning.fotoframe.ShowSlidesFragment
import com.learning.fotoframe.ListPhotosFragmentV2.MyLink
import com.learning.fotoframe.SliderAdapter
import com.squareup.picasso.Picasso
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType
import com.smarteist.autoimageslider.SliderAnimations

class ShowSlidesFragment : Fragment() {
    private var storageReference: StorageReference? = null
    private lateinit var sliderView: SliderView
    private lateinit var appMainViewModel: AppMainViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(requireContext())
        storageReference = FirebaseStorage.getInstance().reference
        appMainViewModel = ViewModelProvider(requireActivity())[AppMainViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_show_slides, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val decorView = requireActivity().window.decorView

        //decorView.systemUiVisibility = flags
        sliderView = view.findViewById(R.id.imageSlider)
        appMainViewModel.mutableLiveDataMyLink.observe(viewLifecycleOwner, Observer { myLinks ->
            if (myLinks == null) return@Observer
            val myLinks2 = myLinks.toTypedArray()
            val sliderAdapter = SliderAdapter(myLinks2) { imageView: ImageView?, position: Int? ->
                Picasso.get()
                    .load(myLinks2[position!!].link)
                    .placeholder(R.drawable.download)
                    .error(R.drawable.close)
                    .into(imageView)
            }
            sliderView.setSliderAdapter(sliderAdapter)
            sliderView.setIndicatorAnimation(IndicatorAnimationType.WORM)
            sliderView.setSliderTransformAnimation(SliderAnimations.DEPTHTRANSFORMATION)
            sliderView.startAutoCycle()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                requireActivity().window.insetsController?.hide(WindowInsets.Type.statusBars())
            } else {
                requireActivity().window.setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
                )
            }

        })
    }

    companion object {
        private val TAG = ShowSlidesFragment::class.java.simpleName
        const val flags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }
}