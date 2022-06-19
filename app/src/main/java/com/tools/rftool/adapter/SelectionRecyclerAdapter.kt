package com.tools.rftool.adapter

import androidx.recyclerview.selection.SelectionTracker

abstract class SelectionRecyclerAdapter<T: ItemDetailsViewHolder>: ItemKeyRecyclerAdapter<T>() {
    protected var tracker: SelectionTracker<Long>? = null
    protected var isInActionMode = false

    fun setSelectionTracker(tracker: SelectionTracker<Long>) {
        this.tracker = tracker
    }

    fun setIsInActionMode(actionMode: Boolean) {
        isInActionMode = actionMode
    }
}