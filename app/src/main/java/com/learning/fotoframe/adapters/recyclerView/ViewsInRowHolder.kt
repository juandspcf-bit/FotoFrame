package com.learning.fotoframe.adapters.recyclerView

import android.view.View
import com.learning.fotoframe.adapters.recyclerView.ViewsInRowHolder

interface ViewsInRowHolder {
    fun fillViews(itemView: View)
    fun getNewInstance(): ViewsInRowHolder
}