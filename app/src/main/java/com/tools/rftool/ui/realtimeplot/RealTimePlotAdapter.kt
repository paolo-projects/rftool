package com.tools.rftool.ui.realtimeplot

import android.view.View
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.collections.ArrayList

class RealTimePlotAdapter {
    private val dataset = ArrayList<RealTimeDataPoint<Double>>()
    private var datasetChanged = AtomicBoolean(false)

    var timeWindowSize: Double = 20.0

    private var attachedView: View? = null

    private var _initialTime: Long? = null
    val initialTime: Long?
        get() = _initialTime

    fun add(data: Double) {
        val nowTime = Date().time
        if(_initialTime == null) {
            _initialTime = nowTime
        }
        dataset.add(RealTimeDataPoint(nowTime, data))

        notifyDatasetChanged()
    }

    protected fun notifyDatasetChanged() {
        datasetChanged.set(true)
        attachedView?.invalidate()
    }

    fun hasDataChanged() = datasetChanged.get()
    fun resetDataChange() = datasetChanged.set(false)

    fun get(): List<RealTimeDataPoint<Double>> {
        attemptRemoveEntries()
        return dataset
    }

    fun clear() {
        dataset.clear()
    }

    fun setAttachedView(view: View) {
        attachedView = view
    }

    private fun attemptRemoveEntries() {
        val nowTime = Date().time
        val timeWindowSize = (timeWindowSize*1000).toInt()
        val timeLimit = nowTime - timeWindowSize
        while(dataset.size > 0 && dataset[0].time < timeLimit) {
            dataset.removeAt(0)
        }
    }
}