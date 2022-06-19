package com.tools.rftool.adapter

import androidx.recyclerview.widget.RecyclerView

abstract class ItemKeyRecyclerAdapter<E: RecyclerView.ViewHolder>: RecyclerView.Adapter<E>() {
    abstract fun getPosition(id: Long): Int
    abstract override fun getItemId(position: Int): Long
}