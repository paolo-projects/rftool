package com.tools.rftool.adapter

import android.view.View
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView

abstract class ItemDetailsViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    abstract fun getItem(): ItemDetailsLookup.ItemDetails<Long>
}