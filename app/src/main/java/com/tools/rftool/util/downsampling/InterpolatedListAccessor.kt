package com.tools.rftool.util.downsampling

import kotlin.math.roundToInt

class InterpolatedListAccessor<E>(private val data: List<E>) {
    var dataRange = Pair(0, 0)
    var viewRange = Pair(0, 0)

    fun get(viewX: Float): E {
        val dataIndex = ((viewX - viewRange.first) / (viewRange.second - viewRange.first) * (dataRange.second - dataRange.first) + dataRange.first).roundToInt()
        return data[dataIndex]
    }

    operator fun get(index: Int): E {
        return get(index.toFloat())
    }
}