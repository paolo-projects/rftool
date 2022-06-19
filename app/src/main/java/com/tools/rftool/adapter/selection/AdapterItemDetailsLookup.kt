package com.tools.rftool.adapter.selection

import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView
import com.tools.rftool.adapter.ItemDetailsViewHolder

class AdapterItemDetailsLookup<T: ItemDetailsViewHolder>(private val recyclerView: RecyclerView): ItemDetailsLookup<Long>() {
    override fun getItemDetails(e: MotionEvent): ItemDetails<Long>? {
        val view = recyclerView.findChildViewUnder(e.x, e.y)
        if(view != null) {
            val viewHolder = (recyclerView.getChildViewHolder(view) as T)
            return viewHolder.getItem()
        }
        return null
    }
}