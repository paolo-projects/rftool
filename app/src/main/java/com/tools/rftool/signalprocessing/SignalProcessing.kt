package com.tools.rftool.signalprocessing

class SignalProcessing(private val listener: Listener) {

    companion object {
        init {
            System.loadLibrary("rftool")
        }
    }

    interface Listener {
        fun onProgressUpdate(progress: Double)
    }

    init {
        init()
    }

    fun destroy() {
        free()
    }

    // Called from JNI
    private fun onProgressUpdate(progress: Double) {
        listener.onProgressUpdate(progress)
    }

    external fun init()
    external fun free()
    external fun filterDataToSignalFrequency(data: DoubleArray, sampleRate: Int, threshold: Double)

}