package com.tools.signalanalysis.utils.downsampling

import com.tools.signalanalysis.adapter.SignalDataPoint
import java.util.Comparator.comparing
import java.util.function.Supplier

internal class Triangle private constructor(
    left: Bucket,
    center: Bucket,
    right: Bucket
) {
    private val left: Bucket
    private val center: Bucket
    private val right: Bucket
    val first: SignalDataPoint
        get() = left.first
    val last: SignalDataPoint
        get() = right.last
    val result: SignalDataPoint
        get() {
            val result: SignalDataPoint = center.map { b: SignalDataPoint? ->
                Area.ofTriangle(
                    left.getResult(),
                    b!!, right.getCenter()
                )
            }
                .maxByOrNull { obj -> obj.getValue() }
                ?.generator
                ?: throw RuntimeException(
                    "Can't obtain max area triangle"
                )

            center.setResult(result)
            return result
        }

    companion object {
        fun of(buckets: List<Bucket>): Triangle {
            return Triangle(
                buckets[0],
                buckets[1],
                buckets[2]
            )
        }
    }

    init {
        this.left = left
        this.center = center
        this.right = right
    }
}