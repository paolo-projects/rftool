package com.tools.rftool.rtlsdr

import kotlin.math.sqrt

data class IQ(var I: Double, var Q: Double) {
    fun magnitude(): Double {
        return sqrt(I*I + Q*Q)
    }
}
