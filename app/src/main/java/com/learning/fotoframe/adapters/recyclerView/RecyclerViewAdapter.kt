package com.learning.fotoframe.adapters.recyclerView

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.learning.fotoframe.adapters.recyclerView.viewHolders.summaryBP.ViewsInMyLinks2RowHolder

class RecyclerViewAdapter(
    private val size: Int,
    private val layout_resource: Int,
    private val onEntityClickListener: (Int , Int )->Unit,
    private val viewsInRowHolder: ViewsInRowHolder,
    private val fillViewsFieldsWithEntitiesValues: (Int, ViewsInRowHolder?, RecyclerViewAdapter)-> Unit
) : RecyclerView.Adapter<GenericViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenericViewHolder {
        val viewRow = LayoutInflater.from(parent.context)
            .inflate(layout_resource, parent, false)
        return GenericViewHolder(
            viewRow,
            onEntityClickListener,
            viewsInRowHolder
        )
    }

    override fun onBindViewHolder(holder: GenericViewHolder, position: Int) {
        fillViewsFieldsWithEntitiesValues(position, holder.viewsInRow, this)
    }

    override fun getItemCount(): Int {
        return size
    }
}