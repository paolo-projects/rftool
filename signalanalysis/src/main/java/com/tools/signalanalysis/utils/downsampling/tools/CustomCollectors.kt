package com.tools.signalanalysis.utils.downsampling.tools

import android.os.Build
import androidx.annotation.RequiresApi
import java.util.stream.Collector


object CustomCollectors {
    @RequiresApi(Build.VERSION_CODES.N)
    fun <T> sliding(size: Int): Collector<T, *, List<List<T>>> {
        return SlidingCollector<T>(size, 1)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun <T> sliding(size: Int, step: Int): Collector<T, *, List<List<T>>> {
        return SlidingCollector<T>(size, step)
    }
}