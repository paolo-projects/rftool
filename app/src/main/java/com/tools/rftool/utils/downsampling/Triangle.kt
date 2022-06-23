package com.tools.rftool.utils.downsampling

import com.tools.rftool.adapter.SignalDataPoint

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