package com.tools.signalanalysis.utils.downsampling

import com.tools.signalanalysis.adapter.SignalDataPoint
import java.util.*


internal class Bucket private constructor(
    private val data: List<SignalDataPoint>,
    val first: SignalDataPoint,
    val last: SignalDataPoint,
    center: SignalDataPoint
) {
    private val center: SignalDataPoint
    private var result: SignalDataPoint? = null

    fun getCenter(): SignalDataPoint {
        return center
    }

    fun getResult(): SignalDataPoint {
        return result ?: first
    }

    fun setResult(value: SignalDataPoint) {
        result = value
    }

    fun <U> map(mapper: (value: SignalDataPoint) -> U): List<U> {
        return data.map(mapper)
    }

    companion object {
        fun of(us: List<SignalDataPoint>): Bucket {
            val first = us[0]
            val last = us[us.size - 1]
            val center = SignalDataPoint(
                first.time + ((last.time - first.time) / 2),
                first.value + ((last.value - first.value) / 2)
            )
            return Bucket(us, first, last, center)
        }

        fun of(u: SignalDataPoint): Bucket {
            return Bucket(Collections.singletonList(u), u, u, u)
        }
    }

    init {
        this.center = center
    }
}