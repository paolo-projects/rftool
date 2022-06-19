package com.tools.rftool.util.text

object DisplayUtils {
    fun formatDurationHumanReadable(seconds: Float): String {
        return if( seconds > 1f) {
            "%.1f s".format(seconds)
        } else if (seconds > 0.001f) {
            "%.0f ms".format(seconds * 1000)
        } else {
            "%.0f us".format(seconds * 1e6)
        }
    }
    fun formatFrequencyHumanReadable(frequency: Int): String {
        return if( frequency > 1000000000) {
            "%.3f GHz".format(frequency / 1e9f)
        } else if (frequency > 1000000) {
            "%.3f MHz".format(frequency / 1e6f)
        } else if (frequency > 1000){
            "%.3f KHz".format(frequency / 1e3f)
        } else {
            "%d Hz".format(frequency)
        }
    }
}