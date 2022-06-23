package com.tools.rftool.adapter

import com.tools.rftool.ui.SignalAnalysisView

abstract class SignalAnalysisViewAdapter {
    private var _view: SignalAnalysisView? = null
    private var _dataChanged = true

    fun setAttachedView(view: SignalAnalysisView) {
        _view = view
    }

    fun hasDataChanged() = _dataChanged

    protected fun notifyDataChanged() {
        _dataChanged = true
        _view?.invalidate()
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