package com.learning.fotoframe.adapters.recyclerView

import android.view.View
import androidx.recyclerview.widget.RecyclerView

class GenericViewHolder(
    viewRow: View,
    onEntityClickListener: (Int , Int )->Unit,
    viewsInRowHolder: ViewsInRowHolder
) : RecyclerView.ViewHolder(viewRow) {
    val viewsInRow: ViewsInRowHolder?

    init {
        viewRow.setOnClickListener {  view ->
            val id = view.id
            val position = adapterPosition
            onEntityClickListener(id, position)

        }

        viewsInRow = viewsInRowHolder.getNewInstance()
        viewsInRow.fillViews(viewRow)
    }


}