package com.learning.fotoframe.adapters.recyclerView.viewHolders.summaryBP

import android.view.View
import android.widget.Button
import android.widget.ImageView
import com.learning.fotoframe.R
import com.learning.fotoframe.adapters.recyclerView.ViewsInRowHolder

class ViewsInMyLinks2RowHolder : ViewsInRowHolder{

    lateinit var deletePhotoRowButton: Button
    lateinit var photoRowImageView: ImageView

    override fun fillViews(itemView: View) {
        deletePhotoRowButton = itemView.findViewById(R.id.deletePhotoRowButton)
        photoRowImageView = itemView.findViewById(R.id.photoRowImageView)
    }

    override fun getNewInstance(): ViewsInRowHolder = ViewsInMyLinks2RowHolder()
}