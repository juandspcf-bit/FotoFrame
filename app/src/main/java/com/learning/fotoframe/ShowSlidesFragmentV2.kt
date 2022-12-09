package com.learning.fotoframe

import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.firebase.FirebaseApp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.learning.fotoframe.databinding.FragmentSettingsBinding
import com.learning.fotoframe.databinding.FragmentShowSlidesBinding
import com.learning.fotoframe.viewmodels.AppMainViewModel
import com.learning.fotoframe.viewmodels.SettingsViewModel
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType
import com.smarteist.autoimageslider.SliderAnimations
import com.smarteist.autoimageslider.SliderView
import com.squareup.picasso.Picasso

class ShowSlidesFragmentV2: Fragment() {
    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var binding: FragmentShowSlidesBinding

    private var storageReference: StorageReference? = null
    private lateinit var sliderView: SliderView
    private lateinit var appMainViewModel: AppMainViewModel
    private lateinit var controller: NavController
    private lateinit var sf: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(requireContext())
        storageReference = FirebaseStorage.getInstance().reference
        appMainViewModel = ViewModelProvider(requireActivity())[AppMainViewModel::class.java]
        settingsViewModel = ViewModelProvider(requireActivity())[SettingsViewModel::class.java]
        controller = NavHostFragment.findNavController(this)

        val sharedPreferences =
            activity?.getSharedPreferences("my_sf", AppCompatActivity.MODE_PRIVATE)
        if(sharedPreferences !=null){
            sf = sharedPreferences
            editor = sf.edit()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_show_slides,
            container,
            false
        )

        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val closeSliderButton = view.findViewById<Button>(R.id.closeSlider)
        sliderView = view.findViewById(R.id.imageSlider)
        appMainViewModel.mutableLiveDataMyLink.observe(viewLifecycleOwner, Observer { myLinks ->
            if (myLinks == null) return@Observer
            val myLinks2 = myLinks.toTypedArray()
            val sliderAdapter = SliderAdapter(myLinks2) { imageView: ImageView?, position: Int? ->

                imageView?.setOnClickListener{ _ ->

                    Log.d(TAG, "onViewCreated: detected")

                    when(closeSliderButton.visibility){
                        View.INVISIBLE -> {closeSliderButton.visibility = View.VISIBLE
                            closeSliderButton.elevation = 50F
                        }
                        else -> {closeSliderButton.visibility = View.INVISIBLE
                        }
                    }
                }

                Picasso.get()
                    .load(myLinks2[position!!].link)
                    .placeholder(R.drawable.download)
                    .error(R.drawable.close)
                    .into(imageView)

            }
            sliderView.setSliderAdapter(sliderAdapter)
            sliderView.setIndicatorAnimation(IndicatorAnimationType.SWAP)

            val delay = sf.getInt("sliderScrollTimeInSec", 0)
            sliderView.scrollTimeInSec = delay

            when(sf.getInt("transitionAnimation", 0)){
                0-> sliderView.setSliderTransformAnimation(SliderAnimations.DEPTHTRANSFORMATION)
                1-> sliderView.setSliderTransformAnimation(SliderAnimations.CUBEINDEPTHTRANSFORMATION)
                2-> sliderView.setSliderTransformAnimation(SliderAnimations.CLOCK_SPINTRANSFORMATION)
                3-> sliderView.setSliderTransformAnimation(SliderAnimations.VERTICALFLIPTRANSFORMATION)
                4-> sliderView.setSliderTransformAnimation(SliderAnimations.CUBEINROTATIONTRANSFORMATION)
            }

            when(sf.getInt("cycle", 0)){
                0-> sliderView.autoCycleDirection = SliderView.AUTO_CYCLE_DIRECTION_RIGHT
                1-> sliderView.autoCycleDirection = SliderView.AUTO_CYCLE_DIRECTION_LEFT
                2-> sliderView.autoCycleDirection = SliderView.AUTO_CYCLE_DIRECTION_BACK_AND_FORTH
            }


            sliderView.startAutoCycle()


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                requireActivity().window.insetsController?.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                requireActivity().window.insetsController?.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                val decorView = requireActivity().window.decorView

                val flags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
                decorView.systemUiVisibility = flags
            }

        })

        closeSliderButton.setOnClickListener{
            controller.popBackStack()
        }


    }

    companion object {
        private val TAG = ShowSlidesFragment::class.java.simpleName

    }
}