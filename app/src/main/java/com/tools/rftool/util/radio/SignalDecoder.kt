package com.tools.rftool.util.radio

import com.tools.rftool.fft.Fft
import kotlin.math.*

class SignalDecoder(private var sampleRate: Int, private var treshold: Double, private var fftSize: Int = 1024) {

    companion object {
        init {
            System.loadLibrary("rftool")
        }
    }

    fun decode(data: ByteArray): DoubleArray {
        val doubleData = DoubleArray(data.size)
        data.forEachIndexed { index, byte ->  doubleData[index] = (byte.toInt() and 0xFF) - 127.5 }
        return doubleData
    }
}