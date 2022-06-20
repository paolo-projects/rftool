package com.tools.signalanalysis.data

import android.view.View
import com.tools.signalanalysis.adapter.SignalDataPoint
import com.tools.signalanalysis.ui.SignalAnalysisView

abstract class SignalAnalysisViewAdapter {
    private var _view: SignalAnalysisView? = null
    private var _dataChanged = true

    fun setAttachedView(view: SignalAnalysisView) {
        _view = view
    }

    fun hasDataChanged() = _dataChanged

    protected fun notifyDataChanged() {
        _dataChanged = true
    }

    fun resetDataChanged() {
        _dataChanged = false
    }

    abstract fun getCountBetween(x0: Int, x1: Int): Int

    abstract fun getYMinMax(): Pair<Double, Double>

    abstract fun getCount(): Int

    abstract fun getAll(): List<SignalDataPoint>

    abstract fun get(index: Int): SignalDataPoint
}