package com.tools.rftool.ui.realtimeplot

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import kotlin.math.*

class RealTimePlot : View {
    companion object {
        private const val TAG = "RealTimePlot"
    }

    init {
        setWillNotDraw(false)
    }

    private var _adapter: RealTimePlotAdapter? = null
    var adapter: RealTimePlotAdapter?
        get() = _adapter
        set(value) {
            _adapter = value
            _adapter!!.setAttachedView(this)
        }

    private var axesPadding = 128
    private var labelsPadding = 16
    private var maxTextSize = axesPadding - labelsPadding * 2
    private var arrowLateralSize = 8
    private var labelsSpacing = 16
    private var labelsTextSize = 24
    private var titleTextSize = 36

    private val blackPaint = Paint().apply {
        color = 0xFF000000.toInt()
    }
    private val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
    }
    private val titleTextPaint = TextPaint(TextPaint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        textSize = titleTextSize.toFloat()
    }
    private val axesTextPaint = TextPaint(TextPaint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        textSize = labelsTextSize.toFloat()
    }

    var title: String = ""

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

        canvas.drawPaint(blackPaint)
        drawTitle(canvas)

        if (_adapter?.hasDataChanged() == true) {
            val data = _adapter!!.get()
            if (data.isNotEmpty()) {
                _adapter!!.resetDataChange()
                val initialTime = _adapter!!.initialTime!!

                val minX = data.first().time - initialTime
                val maxX = data.last().time - initialTime

                var minY = data.first().value
                var maxY = data.first().value

                data.forEach {
                    minY = min(minY, it.value)
                    maxY = max(maxY, it.value)
                }

                drawAxes(canvas, minX.toDouble(), maxX.toDouble(), minY, maxY)

                var i = 1
                var previousX = ((data[0].time - initialTime) - minX).toFloat() / (maxX - minX) * (width - axesPadding * 2) + axesPadding
                var previousY = height - ((data[0].value - minY).toFloat() / (maxY - minY).toFloat() * (height - axesPadding * 2) + axesPadding)
                while(i < data.size) {
                    val x = ((data[i].time - initialTime) - minX).toFloat() / (maxX - minX) * (width - axesPadding * 2) + axesPadding
                    val y = height - ((data[i].value - minY).toFloat() / (maxY - minY).toFloat() * (height - axesPadding * 2) + axesPadding)

                    canvas.drawLine(previousX, previousY, x, y, whitePaint)
                    previousX = x
                    previousY = y
                    i++
                }
            }
        }
    }

    private fun drawTitle(canvas: Canvas) {
        val titleWidth = titleTextPaint.measureText(title)
        canvas.drawText(title, width / 2f - titleWidth / 2f, (axesPadding - titleTextSize) / 2f, titleTextPaint)
    }

    private fun drawAxes(canvas: Canvas, minX: Double, maxX: Double, minY: Double, maxY: Double) {
        // Axes
        canvas.drawLine(
            axesPadding.toFloat(),
            axesPadding.toFloat(),
            axesPadding.toFloat(),
            height.toFloat() - axesPadding,
            whitePaint
        )
        canvas.drawLine(
            axesPadding.toFloat(),
            height.toFloat() - axesPadding,
            width.toFloat() - axesPadding,
            height.toFloat() - axesPadding,
            whitePaint
        )
        // Arrows
        canvas.drawLine(
            axesPadding.toFloat(),
            axesPadding.toFloat(),
            axesPadding.toFloat() - arrowLateralSize / 2f,
            axesPadding.toFloat() + arrowLateralSize,
            whitePaint
        )
        canvas.drawLine(
            axesPadding.toFloat(),
            axesPadding.toFloat(),
            axesPadding.toFloat() + arrowLateralSize / 2f,
            axesPadding.toFloat() + arrowLateralSize,
            whitePaint
        )
        canvas.drawLine(
            width.toFloat() - axesPadding - arrowLateralSize,
            height.toFloat() - axesPadding - arrowLateralSize / 2f,
            width.toFloat() - axesPadding,
            height.toFloat() - axesPadding,
            whitePaint
        )
        canvas.drawLine(
            width.toFloat() - axesPadding - arrowLateralSize,
            height.toFloat() - axesPadding + arrowLateralSize / 2f,
            width.toFloat() - axesPadding,
            height.toFloat() - axesPadding,
            whitePaint
        )

        canvas.save()
        canvas.translate(labelsPadding.toFloat(), axesPadding.toFloat())

        var y = axesPadding
        while (y < height - axesPadding) {
            val yPoint =
                ((height - axesPadding * 2) - (y - axesPadding + labelsTextSize / 2.0)) / (height - axesPadding * 2) * (maxY - minY) + minY
            val yLabel = "%.1f".format(yPoint)

            val staticLayout =
                StaticLayout.Builder.obtain(yLabel, 0, yLabel.length, axesTextPaint, maxTextSize)
                    .setEllipsize(TextUtils.TruncateAt.END)
                    .setAlignment(Layout.Alignment.ALIGN_OPPOSITE)
                    .build()

            staticLayout.draw(canvas)
            canvas.drawLine(
                maxTextSize + labelsPadding / 2f,
                labelsTextSize / 2f,
                maxTextSize.toFloat() + labelsPadding,
                labelsTextSize / 2f,
                whitePaint
            )

            canvas.translate(0f, labelsTextSize.toFloat() + labelsSpacing)
            y += labelsTextSize + labelsSpacing
        }

        canvas.restore()

        var x = axesPadding
        while (x < width - axesPadding) {
            val xPoint =
                (x.toFloat() - axesPadding) / (width - axesPadding * 2) * (maxX - minX) + minX
            val xLabel = "%.0f s".format(xPoint / 1000)
            val textWidth = axesTextPaint.measureText(xLabel)

            canvas.drawLine(
                x.toFloat(),
                height - labelsTextSize - labelsPadding - labelsPadding / 2f,
                x.toFloat(),
                height.toFloat() - axesPadding,
                whitePaint
            )
            canvas.drawText(xLabel, x.toFloat(), height.toFloat() - labelsPadding, axesTextPaint)

            x += textWidth.roundToInt() + labelsSpacing
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)

        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        var width: Int = widthSize
        var height: Int = heightSize.coerceAtLeast(300)

        setMeasuredDimension(width, height)
    }
}