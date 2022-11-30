package com.learning.fotoframe.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.learning.fotoframe.ListPhotosFragmentV2;
import com.learning.fotoframe.PhotoSetListFragment;

import java.util.List;

public class AppMainViewModel extends AndroidViewModel {
    private final MutableLiveData<List<ListPhotosFragmentV2.MyLink>> mutableLiveDataMyLink = new MutableLiveData<>();
    private final MutableLiveData<List<PhotoSetListFragment.MyLink2>> mutableLiveDataMyLink2 = new MutableLiveData<>();
    public final MutableLiveData<Boolean> isDeleting = new MutableLiveData<>(false);

    private final MutableLiveData<List<String>> mutableLiveDataSets = new MutableLiveData<>();

    public AppMainViewModel(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<List<ListPhotosFragmentV2.MyLink>> getMutableLiveDataMyLink() {
        return mutableLiveDataMyLink;
    }

    public void setMutableLiveDataMyLink(List<ListPhotosFragmentV2.MyLink> myLinkList) {
        this.mutableLiveDataMyLink.setValue(myLinkList);
    }

    public MutableLiveData<List<PhotoSetListFragment.MyLink2>> getMutableLiveDataMyLink2() {
        return mutableLiveDataMyLink2;
    }

    public void setMutableLiveDataMyLink2(List<PhotoSetListFragment.MyLink2> myLinkList) {
        this.mutableLiveDataMyLink2.setValue(myLinkList);
    }

    public MutableLiveData<List<String>> getMutableLiveDataSets() {
        return mutableLiveDataSets;
    }

    public void setMutableLiveDataSets(List<String> mutableLiveDataSets) {
        this.mutableLiveDataSets.setValue(mutableLiveDataSets);
    }
}
