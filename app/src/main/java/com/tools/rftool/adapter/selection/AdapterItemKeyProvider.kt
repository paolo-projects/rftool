package com.tools.rftool.adapter.selection

import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.widget.RecyclerView
import com.tools.rftool.adapter.ItemKeyRecyclerAdapter

class AdapterItemKeyProvider<T: ItemKeyRecyclerAdapter<E>, E: RecyclerView.ViewHolder>(private val adapter: T)
    : ItemKeyProvider<Long>(SCOPE_CACHED) {
    override fun getKey(position: Int): Long = adapter.getItemId(position)
    override fun getPosition(key: Long): Int = adapter.getPosition(key)
}