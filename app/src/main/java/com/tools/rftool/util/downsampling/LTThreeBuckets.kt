package com.tools.rftool.util.downsampling

import android.os.Build
import androidx.annotation.RequiresApi
import com.tools.rftool.adapter.SignalDataPoint
import com.tools.rftool.util.downsampling.tools.CustomCollectors.sliding

object LTThreeBuckets {
    @RequiresApi(Build.VERSION_CODES.N)
    fun sorted(input: List<SignalDataPoint>, desiredBuckets: Int): List<SignalDataPoint> {
        return sorted(input, input.size, desiredBuckets)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun sorted(input: List<SignalDataPoint>?, inputSize: Int, desiredBuckets: Int): List<SignalDataPoint> {
        val results: MutableList<SignalDataPoint> = ArrayList()
        OnePassBucketizer.bucketize(input!!, inputSize, desiredBuckets)
            .stream()
            .collect(sliding(3, 1))
            .stream()
            .map(Triangle::of)
            .forEach { triangle ->
                if (results.size == 0) results.add(triangle.first)
                results.add(triangle.result)
                if (results.size == desiredBuckets + 1) results.add(triangle.last)
            }
        return results
    }
}