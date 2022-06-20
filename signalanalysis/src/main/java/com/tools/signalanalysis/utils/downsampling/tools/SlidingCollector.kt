package com.tools.signalanalysis.utils.downsampling.tools

import android.os.Build
import androidx.annotation.RequiresApi
import java.util.*
import java.util.function.BiConsumer
import java.util.function.BinaryOperator
import java.util.function.Supplier
import java.util.stream.Collector
import java.util.stream.Collectors.toList
import kotlin.collections.ArrayList
import kotlin.math.max


@RequiresApi(Build.VERSION_CODES.N)
class SlidingCollector<T>(private val size: Int, private val step: Int) :
    Collector<T, MutableList<List<T>>, List<List<T>>> {
    private val window: Int = max(size, step)
    private val buffer: Queue<T> = ArrayDeque()
    private var totalIn = 0

    override fun supplier(): Supplier<MutableList<List<T>>> {
        return Supplier<MutableList<List<T>>> { ArrayList() }
    }

    override fun accumulator(): BiConsumer<MutableList<List<T>>, T> {
        return BiConsumer<MutableList<List<T>>, T> { lists, t ->
            buffer.offer(t)
            ++totalIn
            if (buffer.size == window) {
                dumpCurrent(lists)
                shiftBy(step)
            }
        }
    }

    override fun finisher(): java.util.function.Function<MutableList<List<T>>, List<List<T>>>? {
        return java.util.function.Function<MutableList<List<T>>, List<List<T>>> { lists ->
            if (!buffer.isEmpty()) {
                val totalOut = estimateTotalOut()
                if (totalOut > lists.size) {
                    dumpCurrent(lists)
                }
            }
            lists
        }
    }

    private fun estimateTotalOut(): Int {
        return max(0, (totalIn + step - size - 1) / step) + 1
    }

    private fun dumpCurrent(lists: MutableList<List<T>>) {
        val batch: List<T> = buffer.stream().limit(size.toLong()).collect(toList())
        lists.add(batch)
    }

    private fun shiftBy(by: Int) {
        for (i in 0 until by) {
            buffer.remove()
        }
    }

    override fun combiner(): BinaryOperator<MutableList<List<T>>> {
        return BinaryOperator<MutableList<List<T>>> { _, _ ->
            throw UnsupportedOperationException(
                "Combining not possible"
            )
        }
    }

    override fun characteristics(): Set<Collector.Characteristics> {
        return EnumSet.noneOf(Collector.Characteristics::class.java)
    }

}