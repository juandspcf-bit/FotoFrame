package com.learning.fotoframe.entities

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Settings {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "productId")
    var id: Int = 0



    @ColumnInfo(name = "productId")
    var sliderScrollTimeInSec: Int


    constructor(id:Int, sliderScrollTimeInSec: Int){
        this.sliderScrollTimeInSec = sliderScrollTimeInSec
    }

}