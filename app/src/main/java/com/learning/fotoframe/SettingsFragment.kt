package com.learning.fotoframe

import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Button
import android.widget.ImageView
import android.widget.NumberPicker
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.learning.fotoframe.databinding.FragmentSettingsBinding
import com.learning.fotoframe.viewmodels.SettingsViewModel
import com.smarteist.autoimageslider.SliderAnimations
import com.smarteist.autoimageslider.SliderView


class SettingsFragment : Fragment() {
    private lateinit var settingsViewModel: SettingsViewModel
    lateinit var binding: FragmentSettingsBinding
    private lateinit var sf: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsViewModel = ViewModelProvider(requireActivity())[SettingsViewModel::class.java]

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


        binding.SetDelayButton.setOnClickListener {
            showBottomDialog()
        }



        val delay = sf.getInt("sliderScrollTimeInSec", 0)
        when(sf.getInt("transitionAnimation", 3)){
            0-> binding.transitionAnimationTextView.text =  getString(R.string.depth_transformation)
            1-> binding.transitionAnimationTextView.text =  getString(R.string.cube_in_depth_transformation)
            2-> binding.transitionAnimationTextView.text =  getString(R.string.clock_spin_transformation)
            3-> binding.transitionAnimationTextView.text =  getString(R.string.vertical_flip_transformation)
            4-> binding.transitionAnimationTextView.text =  getString(R.string.cube_in_rotation_transformation)
        }

        when(sf.getInt("cycle", 0)){
            0-> binding.cycleTextView.text = getString(R.string.right)
            1-> binding.cycleTextView.text = getString(R.string.left)
            2-> binding.cycleTextView.text = getString(R.string.back_and_forth)
        }
        settingsViewModel.sliderScrollTimeInSec.value = delay
        val delay1 = "$delay secs"
        binding.delayTextView.text = delay1


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

    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.option_1 -> {
                editor.apply{
                    putInt("transitionAnimation",  0)
                    settingsViewModel.transitionAnimation.value = 0
                    saveAnimationTransformation(getString(R.string.depth_transformation))
                }
            }
            R.id.option_2 -> {
                editor.apply{
                    putInt("transitionAnimation",  1)
                    settingsViewModel.transitionAnimation.value = 1
                    saveAnimationTransformation(getString(R.string.cube_in_depth_transformation))
                }
            }
            R.id.option_3 -> {
                editor.apply{
                    putInt("transitionAnimation",  2)
                    settingsViewModel.transitionAnimation.value = 2
                    saveAnimationTransformation(getString(R.string.clock_spin_transformation))
                }
            }

            R.id.option_4 -> {
                editor.apply{
                    putInt("transitionAnimation",  3)
                    settingsViewModel.transitionAnimation.value = 4
                    saveAnimationTransformation(getString(R.string.vertical_flip_transformation))
                }
            }

            R.id.option_5 -> {
                editor.apply{
                    putInt("transitionAnimation",  4)
                    settingsViewModel.transitionAnimation.value = 4
                    saveAnimationTransformation(getString(R.string.cube_in_rotation_transformation))
                }
            }

            R.id.cycle_option_1 -> {
                editor.apply{
                    putInt("cycle",  0)
                    binding.cycleTextView.text = getString(R.string.right)
                    commit()
                }
            }

            R.id.cycle_option_2 -> {
                editor.apply{
                    putInt("cycle",  1)
                    binding.cycleTextView.text = getString(R.string.left)
                    commit()
                }
            }

            R.id.cycle_option_3-> {
                editor.apply{
                    putInt("cycle",  2)
                    binding.cycleTextView.text = getString(R.string.back_and_forth)
                    commit()
                }
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

        val delay = sf.getInt("sliderScrollTimeInSec", 0)
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
        }


        dialog.setCancelable(true)
        dialog.setContentView(vista)
        dialog.show()

    }

}