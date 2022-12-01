package com.learning.fotoframe.viewmodels

import android.app.Application
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.learning.fotoframe.ListPhotosFragmentV2.MyLink

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    val transitionAnimation = MutableLiveData<Int>()
    val sliderScrollTimeInSec = MutableLiveData<Int>()

}

