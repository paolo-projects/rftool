package com.tools.rftool.fft

import com.tools.rftool.rtlsdr.IQ

class Fft {
    companion object {
        init {
            System.loadLibrary("rftool")
        }
    }

    /**
     * Computes the FFT of the given input IQ data (complex number),
     * downsampling linearly to the given `n` size (samples)
     *
     * @param data The input data
     * @param n The number of samples in the FFT
     * @return The fourier transform of the input data
     */
    external fun fft(data: DoubleArray, n: Int): DoubleArray
}