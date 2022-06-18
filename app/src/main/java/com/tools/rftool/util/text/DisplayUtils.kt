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
}