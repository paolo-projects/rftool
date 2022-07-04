package com.tools.rftool.rtlsdr

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.util.Log
import com.tools.rftool.util.graphics.ColorMaps
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class RtlSdr(
    deviceIndex: Int,
    private val listener: RtlSdrListener,
    sampleRate: Int,
    centerFrequency: Int,
    ppmError: Int = 0,
    gain: Int = 40,
    colorMap: Int = 0,
    fftSamples: Int = 1024
) {
    companion object {
        private const val TAG = "RtlSdr"

        // Used to load the 'rftool' library on application startup.
        init {
            System.loadLibrary("rftool")
        }
    }

    interface RtlSdrListener {
        fun onFftMax(fftMax: Double)
    }

    open class RtlSdrError(message: String) : Error(message)
    class RtlSdrClosedError(message: String) : RtlSdrError(message)

    private var deviceClosed = false
    private var _bitmap: Bitmap = Bitmap.createBitmap(1024, 600, Bitmap.Config.ARGB_8888)
    private val paint = Paint().apply {
        color = 0xFF000000.toInt()
    }

    init {
        val canvas = Canvas(_bitmap)
        canvas.drawPaint(paint)
        val mapName = ColorMaps.getColorMap(colorMap)

        if (!open(deviceIndex, sampleRate, centerFrequency, ppmError, gain, mapName, fftSamples)) {
            throw RtlSdrError("Failed to open the SDR device")
        }
    }

    private val _bitmapFlow = MutableSharedFlow<Bitmap>()
    val bitmap = _bitmapFlow.asSharedFlow()

    private val ioDispatcher = Dispatchers.IO

    private external fun open(
        fileDescriptor: Int,
        sampleRate: Int,
        centerFrequency: Int,
        ppmError: Int = 0,
        gain: Int = 40,
        colorMap: String,
        fftSamples: Int = 1024
    ): Boolean

    private external fun setSampleRate(sampleRate: Int): Boolean
    private external fun getSampleRate(): Int

    private external fun setCenterFrequency(centerFrequency: Int): Boolean
    private external fun getCenterFrequency(): Int

    private external fun setGain(gain: Int): Boolean
    private external fun getGain(): Int

    private external fun setPpmError(ppmError: Int): Boolean
    private external fun getPpmError(): Int

    private external fun startDataReading(size: Int)
    private external fun stopDataReading()

    private external fun setColorMap(colorMap: String)
    private external fun setFftN(fftN: Int)

    private external fun close()

    // Called from JNI
    private fun notifyBitmapChanged() {
        CoroutineScope(ioDispatcher + Job()).launch {
            _bitmapFlow.emit(_bitmap)
        }
    }

    /**
     * The rftool lib will get the absolute maximum in the fourier transform, excluding the DC bin,
     * and call this method
     *
     * Called from JNI
     */
    private fun notifyFftAbsoluteMax(freqMax: Double) {
        Log.d(TAG, "notifyFftAbsoluteMax: max FFT absolute value is %.1f".format(freqMax))
        listener.onFftMax(freqMax)
    }

    fun resizeBitmap(width: Int, height: Int) {
        _bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(_bitmap)
        canvas.drawPaint(paint)
    }

    fun setDeviceSampleRate(sampleRate: Int) {
        if (!deviceClosed) {
            if (!setSampleRate(sampleRate)) {
                throw RtlSdrError("An error occurred setting the sample rate")
            }
        } else {
            throw RtlSdrClosedError("Device is closed")
        }
    }

    fun getDeviceSampleRate(): Int {
        if (!deviceClosed) {
            return getSampleRate()
        } else {
            throw RtlSdrClosedError("Device is closed")
        }
    }

    fun setDeviceCenterFrequency(centerFrequency: Int) {
        if (!deviceClosed) {
            if (!setCenterFrequency(centerFrequency)) {
                throw RtlSdrError("An error occurred setting the center frequency")
            }
        } else {
            throw RtlSdrClosedError("Device is closed")
        }
    }

    fun getDeviceCenterFrequency(): Int {
        if (!deviceClosed) {
            return getCenterFrequency()
        } else {
            throw RtlSdrClosedError("Device is closed")
        }
    }

    fun setDevicePpmError(ppmError: Int) {
        if (!deviceClosed) {
            if (!setPpmError(ppmError)) {
                throw RtlSdrError("An error occurred setting the ppm error")
            }
        } else {
            throw RtlSdrClosedError("Device is closed")
        }
    }

    fun getDevicePpmError(): Int {
        if (!deviceClosed) {
            return getPpmError()
        } else {
            throw RtlSdrClosedError("Device is closed")
        }
    }

    fun setDeviceGain(gain: Int) {
        if (!deviceClosed) {
            if (!setGain(gain)) {
                throw RtlSdrError("An error occurred setting the gain")
            }
        } else {
            throw RtlSdrClosedError("Device is closed")
        }
    }

    fun getDeviceGain(): Int {
        if (!deviceClosed) {
            return getGain()
        } else {
            throw RtlSdrClosedError("Device is closed")
        }
    }

    fun startDeviceDataCollection(size: Int) {
        if (!deviceClosed) {
            startDataReading(size)
        } else {
            throw RtlSdrClosedError("Device is closed")
        }
    }

    fun stopDeviceDataCollection() {
        if (!deviceClosed) {
            stopDataReading()
        } else {
            throw RtlSdrClosedError("Device is closed")
        }
    }

    fun setFftColorMap(colorMap: Int) {
        if (!deviceClosed) {
            setColorMap(ColorMaps.getColorMap(colorMap))
        }
    }

    fun setDeviceFftN(fftN: Int) {
        if(!deviceClosed) {
            setFftN(fftN)
        }
    }

    fun closeDevice() {
        close()
        deviceClosed = true
    }
}