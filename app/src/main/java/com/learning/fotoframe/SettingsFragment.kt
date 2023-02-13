package com.learning.fotoframe

import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.learning.fotoframe.databinding.FragmentSettingsBinding
import com.learning.fotoframe.viewmodels.SettingsViewModel
import com.smarteist.autoimageslider.SliderAnimations
import com.smarteist.autoimageslider.SliderView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class SettingsFragment : Fragment() {
    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var controller: NavController
    lateinit var binding: FragmentSettingsBinding
    private lateinit var sf: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var auth: FirebaseAuth
    private lateinit var mySettings: MySettings
    private var alreadyRead: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsViewModel = ViewModelProvider(requireActivity())[SettingsViewModel::class.java]
        controller = NavHostFragment.findNavController(this)
        val sharedPreferences =
            activity?.getSharedPreferences("my_sf", AppCompatActivity.MODE_PRIVATE)
        if(sharedPreferences !=null){
            sf = sharedPreferences
            editor = sf.edit()
        }

        auth = Firebase.auth





    }




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_settings,
            container,
            false
        )

        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerForContextMenu(binding.animateTransitionButton)
        registerForContextMenu(binding.cycleButton)
        registerForContextMenu(binding.indicatorAnimationButton)

        binding.SetDelayButton.setOnClickListener {
            showBottomDialog()
        }

        binding.backFromSettingsButton.setOnClickListener {
            controller.popBackStack()
        }

        binding.transitionAnimationTextView.text =""
        binding.cycleTextView.text =""
        binding.indicatorTextView.text = ""
        binding.delayTextView.text = ""

        val user = auth.currentUser
        if (user==null){
            Toast.makeText(context, "Sign Up before", Toast.LENGTH_SHORT).show()
            mySettings = MySettings(0, 0 , 0)
            return
        }
        val userEmail = user.email

        FirebaseUtilsApp().getSettings(userEmail){
            mySettings = it
            Log.d("Settings Fragment", "onCreate: $mySettings")


            val delay1 = "${mySettings.delay} secs"
            binding.delayTextView.text = delay1

            if (alreadyRead) return@getSettings

            val coroutineScope = CoroutineScope(Dispatchers.Main.immediate)
            coroutineScope.launch {
                when(mySettings.transitionAnimation){
                    0-> binding.transitionAnimationTextView.text =  getString(R.string.depth_transformation)
                    1-> binding.transitionAnimationTextView.text =  getString(R.string.cube_in_depth_transformation)
                    2-> binding.transitionAnimationTextView.text =  getString(R.string.clock_spin_transformation)
                    3-> binding.transitionAnimationTextView.text =  getString(R.string.vertical_flip_transformation)
                    4-> binding.transitionAnimationTextView.text =  getString(R.string.cube_in_rotation_transformation)
                    else -> binding.transitionAnimationTextView.text =""
                }

                when(mySettings.cycle){
                    0-> binding.cycleTextView.text = getString(R.string.right)
                    1-> binding.cycleTextView.text = getString(R.string.left)
                    2-> binding.cycleTextView.text = getString(R.string.back_and_forth)
                    else -> binding.cycleTextView.text =""
                }

                when(mySettings.indicator){
                    0-> binding.indicatorTextView.text =  getString(R.string.slide)
                    1-> binding.indicatorTextView.text =  getString(R.string.scale)
                    2-> binding.indicatorTextView.text =  getString(R.string.swap)
                    3-> binding.indicatorTextView.text =  getString(R.string.drop)
                    4-> binding.indicatorTextView.text =  getString(R.string.color)
                    else -> binding.indicatorTextView.text = ""
                }
            }

            alreadyRead = true



        }


    }


    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        if(v.id==R.id.animateTransitionButton){
            menu.setHeaderTitle("Pick option")
            requireActivity().menuInflater.inflate(R.menu.menu_efect_transition, menu)
        }

        if(v.id==R.id.cycleButton){
            menu.setHeaderTitle("Pick option")
            requireActivity().menuInflater.inflate(R.menu.cycle_options_menu, menu)
        }

        if(v.id==R.id.indicatorAnimationButton){
            menu.setHeaderTitle("Pick option")
            requireActivity().menuInflater.inflate(R.menu.menu_indicator_animation, menu)
        }

    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val firebaseUtilsApp = FirebaseUtilsApp()
        val user = auth.currentUser
        if (user==null){
            Toast.makeText(context, "Sign Up before", Toast.LENGTH_SHORT).show()
            mySettings = MySettings(0, 0 , 0)
            return false
        }
        val userEmail = user.email
        when (item.itemId) {
            R.id.option_1 -> {
                editor.apply{
                    putInt("transitionAnimation",  0)
                    settingsViewModel.transitionAnimation.value = 0
                    saveAnimationTransformation(getString(R.string.depth_transformation))
                }

                mySettings.transitionAnimation = 0
                firebaseUtilsApp.addSettingsToDataBase(userEmail, mySettings)

            }
            R.id.option_2 -> {
                editor.apply{
                    putInt("transitionAnimation",  1)
                    settingsViewModel.transitionAnimation.value = 1
                    saveAnimationTransformation(getString(R.string.cube_in_depth_transformation))
                    mySettings.transitionAnimation = 1
                    firebaseUtilsApp.addSettingsToDataBase(userEmail, mySettings)
                }
            }
            R.id.option_3 -> {
                editor.apply{
                    putInt("transitionAnimation",  2)
                    settingsViewModel.transitionAnimation.value = 2
                    saveAnimationTransformation(getString(R.string.clock_spin_transformation))
                    mySettings.transitionAnimation = 2
                    firebaseUtilsApp.addSettingsToDataBase(userEmail, mySettings)
                }
            }

            R.id.option_4 -> {
                editor.apply{
                    putInt("transitionAnimation",  3)
                    settingsViewModel.transitionAnimation.value = 4
                    saveAnimationTransformation(getString(R.string.vertical_flip_transformation))
                    mySettings.transitionAnimation = 3
                    firebaseUtilsApp.addSettingsToDataBase(userEmail, mySettings)
                }
            }

            R.id.option_5 -> {
                editor.apply{
                    putInt("transitionAnimation",  4)
                    settingsViewModel.transitionAnimation.value = 4
                    saveAnimationTransformation(getString(R.string.cube_in_rotation_transformation))
                    mySettings.transitionAnimation = 4
                    firebaseUtilsApp.addSettingsToDataBase(userEmail, mySettings)
                }
            }

            R.id.cycle_option_1 -> {
                editor.apply{
                    putInt("cycle",  0)
                    binding.cycleTextView.text = getString(R.string.right)
                    commit()
                }
                mySettings.cycle = 0
                firebaseUtilsApp.addSettingsToDataBase(userEmail, mySettings)
            }

            R.id.cycle_option_2 -> {
                editor.apply{
                    putInt("cycle",  1)
                    binding.cycleTextView.text = getString(R.string.left)
                    commit()
                }

                mySettings.cycle = 1
                firebaseUtilsApp.addSettingsToDataBase(userEmail, mySettings)
            }

            R.id.cycle_option_3-> {
                editor.apply{
                    putInt("cycle",  2)
                    binding.cycleTextView.text = getString(R.string.back_and_forth)
                    commit()
                }

                mySettings.cycle = 2
                firebaseUtilsApp.addSettingsToDataBase(userEmail, mySettings)
            }

            R.id.indicator_1-> {
                editor.apply{
                    putInt("indicator",  0)
                    binding.indicatorTextView.text = getString(R.string.slide)
                    commit()
                }

                mySettings.indicator = 0
                firebaseUtilsApp.addSettingsToDataBase(userEmail, mySettings)
            }

            R.id.indicator_2-> {
                editor.apply{
                    putInt("indicator", 1)
                    binding.indicatorTextView.text = getString(R.string.scale)
                    commit()
                }
                mySettings.indicator = 1
                firebaseUtilsApp.addSettingsToDataBase(userEmail, mySettings)
            }

            R.id.indicator_3-> {
                editor.apply{
                    putInt("indicator", 2)
                    binding.indicatorTextView.text = getString(R.string.swap)
                    commit()
                }
                mySettings.indicator = 2
                firebaseUtilsApp.addSettingsToDataBase(userEmail, mySettings)
            }

            R.id.indicator_4-> {
                editor.apply{
                    putInt("indicator", 3)
                    binding.indicatorTextView.text = getString(R.string.drop)
                    commit()
                }
                mySettings.indicator = 3
                firebaseUtilsApp.addSettingsToDataBase(userEmail, mySettings)
            }

            R.id.indicator_5-> {
                editor.apply{
                    putInt("indicator", 4)
                    binding.indicatorTextView.text = getString(R.string.color)
                    commit()
                }

                mySettings.indicator = 4
                firebaseUtilsApp.addSettingsToDataBase(userEmail, mySettings)
            }

        }
        return super.onContextItemSelected(item)
    }

    private fun SharedPreferences.Editor.saveAnimationTransformation(type: String) {
        binding.transitionAnimationTextView.text = type
        commit()
    }

    private fun showBottomDialog() {
        val dialog = BottomSheetDialog(requireContext())
        val vista = LayoutInflater.from(context).inflate(R.layout.selec_digits_dialog, null)
        val numberPicker = vista.findViewById<NumberPicker>(R.id.delaySecondsNumberPicker)
        val setDelayButton = vista.findViewById<Button>(R.id.setDelaybutton)


        numberPicker.maxValue = 50
        numberPicker.minValue = 1

        val delay = mySettings.delay
        settingsViewModel.sliderScrollTimeInSec.value = delay



        numberPicker.value = delay
        setDelayButton.setOnClickListener {
            editor.apply{
                putInt("sliderScrollTimeInSec",  numberPicker.value)
                settingsViewModel.sliderScrollTimeInSec.value = numberPicker.value
                val delay1 = "${numberPicker.value} secs"
                binding.delayTextView.text = delay1
                commit()
                dialog.dismiss()
            }

            val firebaseUtilsApp = FirebaseUtilsApp()
            val user = auth.currentUser
            if (user==null){
                Toast.makeText(context, "Sign Up before", Toast.LENGTH_SHORT).show()
                mySettings = MySettings(0, 0 , 0)
                return@setOnClickListener
            }
            val userEmail = user.email
            if (numberPicker.value.toString().toInt()>=3){
                mySettings.delay = numberPicker.value
                firebaseUtilsApp.addSettingsToDataBase(userEmail, mySettings)
            }

        }


        dialog.setCancelable(true)
        dialog.setContentView(vista)
        dialog.show()

    }

    data class MySettings(
        var transitionAnimation: Int = 0,
        var cycle: Int = 0,
        var indicator: Int = 0,
        var delay: Int = 3

    )


    override fun onResume() {
        super.onResume()
        val user = auth.currentUser
        if (user==null){
            Toast.makeText(context, "Sign Up before", Toast.LENGTH_SHORT)
            return
        }
        val userEmail = user.email


    }
}