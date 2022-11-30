package com.learning.fotoframe

import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.NumberPicker
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.learning.fotoframe.databinding.FragmentSettingsBinding
import com.learning.fotoframe.viewmodels.SettingsViewModel


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

        binding.SetDelayButton.setOnClickListener {
            showBottomDialog()
        }

        val delay = sf.getInt("sliderScrollTimeInSec", 0)
        settingsViewModel.sliderScrollTimeInSec.value = delay
        val delay1 = "$delay secs"
        binding.delayTextView.text = delay1

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