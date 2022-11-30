package com.learning.fotoframe.adapters.recyclerView.viewHolders.summaryBP

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.learning.fotoframe.R
import com.learning.fotoframe.adapters.recyclerView.ViewsInRowHolder


class ViewsInMyLinkRowHolder : ViewsInRowHolder {
    lateinit var rowSetNameTextView: TextView
    lateinit var rowSetImageView: ImageView
    lateinit var rowPlayButton: Button
    lateinit var rowSetDeleteButton: Button
    override fun fillViews(itemView: View) {

        rowSetNameTextView = itemView.findViewById<TextView>(R.id.rowSetNameTextView)
        rowSetImageView = itemView.findViewById<ImageView>(R.id.rowSetImageView)
        rowPlayButton = itemView.findViewById<Button>(R.id.buttonPlay)
        rowSetDeleteButton = itemView.findViewById<Button>(R.id.deleteSetButton)

    }

    override fun getNewInstance(): ViewsInRowHolder = ViewsInMyLinkRowHolder()


}