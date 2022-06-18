package com.tools.rftool.ui.powerbar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import java.util.*
import kotlin.math.*

class PowerBar: View {
    companion object {
        private const val TAG = "PowerBar"
        private const val MAX_RECALC_DELAY = 3000
    }

    init {
        setWillNotDraw(false)
    }

    var min: Double = 0.0
    var max: Double = sqrt(127.5*127.5 * 2)
    private var lastMaxValueAt = Date().time

    private var _value: Double = 0.0
    var value: Double
        get() = _value
        set(newValue) {
            _value = newValue
            invalidate()
        }

    private val paint = Paint().apply {
        color = 0xFFFFFFFF.toInt()
    }
    private val redPaint = Paint().apply {
        color = 0xFFFF0000.toInt()
    }

    private val barWidth = 64
    private val dashWidth = 20
    private val textHeight = 24
    private val textPadding = 16

    var treshold: Float? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    )

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val nowTime = Date().time
        if(value > max || nowTime > lastMaxValueAt + MAX_RECALC_DELAY) {
            max = value + 10
            lastMaxValueAt = Date().time
        }

        paint.color = 0xFF000000.toInt()
        canvas.drawPaint(paint)

        paint.color = 0xFFFFFFFF.toInt()

        val powerAmount = height.toFloat() / (max - min).toFloat() * value.toFloat()
        canvas.drawRect(0f, height - powerAmount, barWidth.toFloat(), height.toFloat(), paint)

        val textPaint = TextPaint(TextPaint.ANTI_ALIAS_FLAG)
        textPaint.textSize = textHeight.toFloat()
        textPaint.color = 0xFFFFFFFF.toInt()

        var y = textPadding
        while(y < height - textPadding) {
            canvas.drawRect(barWidth.toFloat() + textPadding, y.toFloat() + textHeight / 2f - 2, barWidth.toFloat() + textPadding + dashWidth, y.toFloat() + textHeight / 2f + 2, paint)
            val value = (height - (y + textHeight/2.0)) / height * (max - min) + min
            val valueString = "%.0f".format(value)
            canvas.drawText(valueString, barWidth.toFloat() + textPadding * 2 + dashWidth, y.toFloat() + textHeight, textPaint)
            y += textHeight + textPadding
        }

        if(treshold != null) {
            val tresholdY = height - textHeight/2f - height * (treshold!!-min).toFloat()/(max-min)
            if(tresholdY > 0 && tresholdY < height) {
                canvas.drawRect(0f, max(0f, tresholdY.toFloat() - 2f), barWidth.toFloat(), min(height.toFloat(), tresholdY.toFloat() + 2f), redPaint)
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)

        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        var width: Int = 0
        var height: Int = 0

        val textPainter = TextPaint(TextPaint.ANTI_ALIAS_FLAG).apply {
            textSize = textHeight.toFloat()
        }

        val maxText = "%.0f".format(max)
        val maxTextWidth = textPainter.measureText(maxText)

        width = ceil(barWidth + textPadding*2 + dashWidth + maxTextWidth + textPadding).toInt()
        height = heightSize

        setMeasuredDimension(width, height)
    }
}