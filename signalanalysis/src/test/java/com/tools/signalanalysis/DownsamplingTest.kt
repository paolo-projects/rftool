package com.tools.signalanalysis

import com.tools.signalanalysis.adapter.SignalDataPoint
import com.tools.signalanalysis.utils.downsampling.LTThreeBuckets
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class DownsamplingTest {
    @Test
    fun downsamplingCorrectSize() {
        val testData = Array(5000) { SignalDataPoint(0, 0.0) }
        val decimated = LTThreeBuckets.sorted(testData.toList(), 1022)
        assertEquals(1024, decimated.size)
    }
}