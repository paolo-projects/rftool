package com.tools.rftool.ui.chart

import com.tools.rftool.adapter.SignalDataPoint
import com.tools.rftool.adapter.SignalAnalysisViewAdapter
import kotlin.math.max
import kotlin.math.min

class SignalAnalysisAdapter: SignalAnalysisViewAdapter() {
    private val dataset = ArrayList<SignalDataPoint>()

    fun set(data: List<SignalDataPoint>) {
        dataset.clear()
        dataset.addAll(data)
        notifyDataChanged()
    }

    fun clear() {
        dataset.clear()
        notifyDataChanged()
    }

    fun add(data: List<SignalDataPoint>) {
        dataset.addAll(data)
    }

    override fun getCountBetween(x0: Int, x1: Int): Int {
        return dataset.fold(0) { acc, p ->
            if(p.time in x0..x1) acc + 1 else acc
        }
    }

    override fun getYMinMax(): Pair<Double, Double> {
        var yMin = dataset.first().value
        var yMax = yMin
        for(entry in dataset) {
            yMin = min(yMin, entry.value)
            yMax = max(yMax, entry.value)
        }
        return Pair(yMin, yMax)
    }

    override fun getCount(): Int {
        return dataset.count()
    }

    override fun getAll(): List<SignalDataPoint> {
        return dataset
    }

    override fun get(index: Int): SignalDataPoint {
        return dataset[index]
    }
}