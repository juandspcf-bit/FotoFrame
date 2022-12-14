package com.learning.fotoframe.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.learning.fotoframe.ListPhotosFragmentV2.MyLink
import com.learning.fotoframe.PhotoSetListFragment.MyLink2

class AppMainViewModel(application: Application) : AndroidViewModel(application) {
    val mutableLiveDataMyLink = MutableLiveData<List<MyLink>>()
    val mutableLiveDataMyLink2 = MutableLiveData<MutableList<MyLink2>>()
    val isDeleting = MutableLiveData(false)
    val mutableLiveDataSets = MutableLiveData<MutableList<String>>()
    fun setMutableLiveDataMyLink(myLinkList: List<MyLink>) {
        mutableLiveDataMyLink.value = myLinkList
    }

    fun setMutableLiveDataMyLink2(myLinkList: MutableList<MyLink2>) {
        mutableLiveDataMyLink2.value = myLinkList
    }

    fun setMutableLiveDataSets(mutableLiveDataSets: MutableList<String>) {
        this.mutableLiveDataSets.value = mutableLiveDataSets
    }
}