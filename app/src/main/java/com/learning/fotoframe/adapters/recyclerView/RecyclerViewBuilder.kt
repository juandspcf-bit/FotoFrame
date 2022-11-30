package com.learning.fotoframe.adapters.recyclerView

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager

class RecyclerViewBuilder(
    private val sizeEntities: Int,
    private val context: Context?,
    private val recyclerView: RecyclerView,
    private val layout_resource: Int,
    private val onEntityClickListener: (Int , Int )->Unit,
    private var fillViewsFieldsWithEntitiesValues: (Int, ViewsInRowHolder?, RecyclerViewAdapter)-> Unit,
    private val viewsInRowHolder: ViewsInRowHolder
) {
    fun buildWithLinearLayoutManager() {
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)
        val recyclerViewAdapter = RecyclerViewAdapter(
            sizeEntities,
            layout_resource,
            onEntityClickListener,
            viewsInRowHolder,
            fillViewsFieldsWithEntitiesValues
        )
        recyclerView.adapter = recyclerViewAdapter
    }

    fun buildWithGridLayoutManager() {
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = GridLayoutManager(
            context,
            3,
            LinearLayoutManager.VERTICAL,
            false
        )
        val recyclerViewAdapter = RecyclerViewAdapter(
            sizeEntities,
            layout_resource,
            onEntityClickListener,
            viewsInRowHolder,
            fillViewsFieldsWithEntitiesValues
        )
        recyclerView.adapter = recyclerViewAdapter
    }
}