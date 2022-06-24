package com.tools.rftool.util.downsampling

import com.tools.rftool.adapter.SignalDataPoint
import kotlin.math.abs

internal class Area<T : SignalDataPoint?> private constructor(val generator: T, private val value: Double) :
  Comparable<Area<T>> {
  fun getValue(): Double {
    return value
  }

  override operator fun compareTo(other: Area<T>): Int {
    return value.compareTo(other.value)
  }

  companion object {
    fun ofTriangle(a: SignalDataPoint, b: SignalDataPoint, c: SignalDataPoint): Area<SignalDataPoint> {
      // area of a triangle = |[Ax(By - Cy) + Bx(Cy - Ay) + Cx(Ay - By)] / 2|
      val addends: List<Double> = listOf(
        a.time * (b.value - c.value),
        b.time * (c.value - a.value),
        c.time * (a.value - b.value)
      )
      val sum = addends.fold(0.0) { acc, value -> acc + value }
      val half = sum / 2.0
      val value = abs(half)
      return Area(b, value)
    }
  }
}