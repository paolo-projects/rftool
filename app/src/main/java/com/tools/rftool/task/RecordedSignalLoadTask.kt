package com.tools.rftool.task

import android.content.Context
import com.tools.rftool.model.Recording
import com.tools.rftool.util.radio.SignalDecoder
import com.tools.rftool.adapter.SignalDataPoint
import com.tools.rftool.signalprocessing.SignalProcessing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import kotlin.collections.ArrayList

class RecordedSignalLoadTask(private val context: Context, private val recording: Recording, private val threshold: Double, private val listener: Listener) :
    SignalProcessing.Listener {
    private val signalDecoder = SignalDecoder(recording.sampleRate, threshold)

    interface Listener {
        fun onProgressUpdate(progress: Double)
    }

    override fun onProgressUpdate(progress: Double) {
        listener.onProgressUpdate(progress)
    }

    suspend fun readFromFile(): List<SignalDataPoint> {
        val file = File("${context.filesDir}/recordings", recording.fileName)
        val data = ArrayList<SignalDataPoint>()

        val bytes = withContext(Dispatchers.IO) {
            val data = ArrayList<Byte>()
            val totalSize = file.length()

            FileInputStream(file).use { stream ->
                val buffer = ByteArray(2048)
                var readSize = 0

                while(stream.available() > 0) {
                    val readBytes =  stream.read(buffer)
                    data.addAll(
                        buffer.sliceArray(0 until readBytes).toList()
                    )
                    readSize += readBytes
                    onProgressUpdate(readSize.toDouble() / totalSize * 0.33)
                }
            }

            data.toByteArray()
        }

        val result = signalDecoder.decode(bytes)

        // Need a better bandpass implementation
        // I'm not good at DSP

        val signalProcessing = SignalProcessing(this)
        signalProcessing.filterDataToSignalFrequency(result, recording.sampleRate, threshold)
        signalProcessing.destroy()

        for (i in result.indices step 2) {
            data.add(SignalDataPoint((i / 2).toLong(), result[i]))
        }

        return data
    }
}