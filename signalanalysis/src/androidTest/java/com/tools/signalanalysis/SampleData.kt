package com.tools.signalanalysis

import com.tools.rftool.adapter.SignalDataPoint
import java.util.*
import kotlin.streams.toList

object SampleData {
    private const val seed = 45645603L

    fun generate(size: Int): List<SignalDataPoint> {
        var i = 0
        return Random(seed).doubles(size.toLong()).mapToObj {
            SignalDataPoint((i++).toLong(), it * 255.0 - 127.5)
        }.toList()
    }
}