package com.tools.rftool.ui.spectrogram

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import kotlin.math.round
import kotlin.math.sqrt

class SpectrogramBitmap(private var width: Int, private var height: Int) {
    companion object {
        private const val MAX_TIME_VALUES = 128
    }

    private var _bitmapBackBuffer: Bitmap
    private var _bitmapFrontBuffer: Bitmap

    val bitmap
        get() = _bitmapFrontBuffer

    private val whitePaint = Paint().apply {
        color = (0xFFFFFFFF).toInt()
    }

    init {
        _bitmapBackBuffer = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        _bitmapFrontBuffer = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    }

    fun resize(width: Int, height: Int) {
        _bitmapBackBuffer = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        _bitmapFrontBuffer = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(_bitmapBackBuffer)
        val canvas2 = Canvas(_bitmapFrontBuffer)
        canvas.drawPaint(whitePaint)
        canvas2.drawPaint(whitePaint)

        this.width = width
        this.height = height
    }

    fun addFftEntry(data: DoubleArray) {
        val rowSize = data.size
        val pixelBuffer = IntArray(_bitmapBackBuffer.width * _bitmapBackBuffer.height - 1)
        // Move the pixels down by 1
        _bitmapBackBuffer.getPixels(
            pixelBuffer,
            0,
            _bitmapBackBuffer.width,
            0,
            0,
            _bitmapBackBuffer.width,
            _bitmapBackBuffer.height - 1
        )
        _bitmapBackBuffer.setPixels(
            pixelBuffer,
            0,
            _bitmapBackBuffer.width,
            0,
            1,
            _bitmapBackBuffer.width,
            _bitmapBackBuffer.height - 1
        )

        val newRow = IntArray(_bitmapBackBuffer.width)
        for (x in 0 until _bitmapBackBuffer.width) {
            val index = round(x.toDouble() / _bitmapBackBuffer.width * (rowSize - 1)).toInt()
            val intensity = (getMagnitude(data, index) / 10 * 0xFF).toUInt()
            newRow[x] =
                ((intensity shl 16) or (intensity shl 8) or (intensity) or (0xFFu shl 24)).toInt()
        }
        _bitmapBackBuffer.setPixels(newRow, 0, _bitmapBackBuffer.width, 0, 0, _bitmapBackBuffer.width, 1)

        synchronized(_bitmapFrontBuffer) {
            // Here we swap the buffers
            val frontBuffer = _bitmapFrontBuffer
            _bitmapFrontBuffer = _bitmapBackBuffer
            _bitmapBackBuffer = frontBuffer
            /*val pixels = IntArray(_bitmapBackBuffer.width * _bitmapBackBuffer.height)
            _bitmapBackBuffer.getPixels(pixels, 0, _bitmapBackBuffer.width, 0, 0, _bitmapBackBuffer.width, _bitmapBackBuffer.height)
            _bitmapFrontBuffer.setPixels(pixels, 0, _bitmapFrontBuffer.width, 0, 0, _bitmapFrontBuffer.width, _bitmapFrontBuffer.height)*/
        }
    }

    private fun getMagnitude(array: DoubleArray, index: Int): Double {
        val real = array[index]
        val imag = array[index+1]
        return sqrt(real*real + imag*imag)
    }
}