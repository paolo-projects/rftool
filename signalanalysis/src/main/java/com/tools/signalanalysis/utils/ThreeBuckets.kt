package com.tools.signalanalysis.utils

import com.tools.signalanalysis.adapter.SignalDataPoint
import java.util.*
import kotlin.collections.ArrayList

class ThreeBuckets(private val downSampledSize: Int) {
    companion object {
        init {
            System.loadLibrary("signalanalysis")
        }
    }

    init {
        init(downSampledSize)
    }

    private external fun init(downSampledSize: Int)

    external fun downSample(data: Array<SignalDataPoint>, outData: Array<SignalDataPoint>)
}