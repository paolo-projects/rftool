package com.tools.rftool.util.graphics

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.text.StaticLayout
import android.text.TextPaint

fun Canvas.drawFftParams(sampleRate: Int, centerFrequency: Int, boxHeight: Int = 56, textSize: Int = 24) {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint.color = 0xFF000000.toInt()

    // Draw a black rectangle at the bottom
    drawRect(0f, height - boxHeight.toFloat(), width.toFloat(), height.toFloat(), paint)

    val lowFreq = centerFrequency - sampleRate / 2
    val highFreq = centerFrequency + sampleRate / 2

    val lowFreqText = "%.3f Mhz".format(lowFreq / 1e6)
    val centerFreqText = "%.3f Mhz".format(centerFrequency / 1e6)
    val highFreqText = "%.3f Mhz".format(highFreq / 1e6)

    val textPaint = TextPaint(TextPaint.ANTI_ALIAS_FLAG)
    textPaint.textSize = textSize.toFloat()
    textPaint.color = 0xFFFFFFFF.toInt()

    drawText(lowFreqText, 16f, height - 16f, textPaint)

    val cfTextWidth = textPaint.measureText(centerFreqText)
    drawText(centerFreqText, width / 2 - cfTextWidth / 2, height - 16f, textPaint)

    val hfTextWidth = textPaint.measureText(highFreqText)
    drawText(highFreqText, width - hfTextWidth - 16f, height - 16f, textPaint)

    drawRect(0f, height - boxHeight - 16f, 4f, height - boxHeight.toFloat(), paint)
    drawRect(width / 2f - 2f, height - boxHeight - 16f, width / 2f + 2f, height - boxHeight.toFloat(), paint)
    drawRect(width - 4f, height - boxHeight - 16f, width.toFloat(), height - boxHeight.toFloat(), paint)
}