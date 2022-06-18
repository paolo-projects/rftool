package com.tools.rftool.util.radio

import com.tools.rftool.fft.Fft
import kotlin.math.*

class SignalDecoder(private var sampleRate: Int, private var fftSize: Int = 1024) {

    companion object {
        init {
            System.loadLibrary("rftool")
        }
    }

    private val fft = Fft()

    fun decode(data: ByteArray): List<Double> {
        val doubleData = DoubleArray(data.size)
        data.forEachIndexed { index, byte ->  doubleData[index] = (byte.toInt() and 0xFF) - 127.5 }

        val finalData = ArrayList<Double>()
        for(i in data.indices step 2) {
            finalData.add(doubleData[i])
            //finalData.add(sqrt(doubleData[i].pow(2) + doubleData[i+1].pow(2)))
        }

        return finalData

        //val frequency = findSignalFrequency(doubleData)
        val fftIndex = findSignalIndex(doubleData)
        val signalData = ArrayList<Double>()

        for(i in 0 until doubleData.size - fftSize * 2 step fftSize * 2) {
            val fftData = fft.fft(doubleData.copyOfRange(i, min(i + fftSize*2, doubleData.size)), fftSize)
            val fftPowers = DoubleArray(fftSize)
            for(n in fftData.indices step 2) {
                fftPowers[n/2] = sqrt(fftData[n].pow(2) + fftData[n+1].pow(2))
            }
            signalData.add(fftPowers[fftIndex])
        }

        return signalData
    }

    private fun findSignalIndex(data:DoubleArray): Int {
        val signalMaxIndices = ArrayList<Int>()

        // Find the frequency of the signal (the signal frequency is given by the peak in the FFT,
        // averaged through all the iterations)
        val fftPower = DoubleArray(fftSize)
        for(i in data.indices step (fftSize*2)) {
            val fftData = fft.fft(data.copyOfRange(i, min(i + fftSize * 2, data.size)), fftSize)
            for(n in fftData.indices step 2) {
                fftPower[n / 2] = sqrt(fftData[n].pow(2) + fftData[n+1].pow(2))
            }
            var maxIndex = 1
            var max = 0.0
            for(n in 1 until fftPower.size) {
                if(fftPower[n] > max) {
                    maxIndex = n
                    max = fftPower[n]
                }
            }
            signalMaxIndices.add(maxIndex)
        }

        return signalMaxIndices.average().roundToInt()
    }

    private fun findSignalFrequency(data: DoubleArray): Double {
        val maxIndex = findSignalIndex(data)
        return if(maxIndex < fftSize / 2) {
            (maxIndex-1).toDouble()/(fftSize / 2 - 1) * sampleRate
        } else {
            -(maxIndex - fftSize / 2).toDouble() / (fftSize / 2) * sampleRate
        }
    }

    private external fun applyButterworth(data: DoubleArray): DoubleArray
}