package com.tools.rftool.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.annotation.RequiresApi
import com.tools.rftool.R
import com.tools.rftool.adapter.SignalDataPoint
import com.tools.rftool.data.DataRange
import com.tools.rftool.adapter.SignalAnalysisViewAdapter
import com.tools.rftool.util.downsampling.InterpolatedListAccessor
import kotlin.math.roundToInt

class SignalAnalysisView : View {
    companion object {
        private const val TAG = "SignalAnalysisView"
    }

    init {
        setWillNotDraw(false)
    }

    private var _adapter: SignalAnalysisViewAdapter? = null
    var adapter: SignalAnalysisViewAdapter?
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
    private var downSampledSize = 1024
    private val defaultDownSampledSize = 1024
    private var isTouching = false

    private var dataRange = DataRange()

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

    private fun applyAttributes(attrs: AttributeSet, defStyleAttr: Int = 0, defStyleRes: Int = 0) {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.SignalAnalysisView,
            defStyleAttr,
            defStyleRes
        ).apply {
            try {
                title = getString(R.styleable.SignalAnalysisView_title) ?: ""
            } finally {
                recycle()
            }
        }
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        applyAttributes(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        applyAttributes(attrs, defStyleAttr)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    ) {
        applyAttributes(attrs, defStyleAttr, defStyleRes)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (_adapter != null) {//_adapter?.hasDataChanged() == true) {
            _adapter!!.resetDataChanged()
            canvas.drawPaint(blackPaint)
            drawTitle(canvas)

            if (_adapter!!.getCount() > 1) {
                val minX: Int
                val maxX: Int
                val minY: Double
                val maxY: Double

                val minMax = _adapter!!.getYMinMax()
                minY = minMax.first
                maxY = minMax.second

                val firstX = _adapter!!.get(0).time
                val lastX = _adapter!!.get(_adapter!!.getCount() - 1).time
                minX = (((lastX - firstX) * dataRange.min).roundToInt() + firstX).toInt()
                maxX = (((lastX - firstX) * dataRange.max).roundToInt() + firstX).toInt()

                if (!isTouching) {
                    drawPoints(canvas, minX, maxX, minY, maxY)
                }
                drawAxes(canvas, minX, maxX, minY, maxY)
            }
        }
    }

    private fun drawPoints(canvas: Canvas, minX: Int, maxX: Int, minY: Double, maxY: Double) {
        /* Draw as many points as the pixels on the screen (or a multiple of it) */
        val data = _adapter!!.getAll()
        val interpolatedData = InterpolatedListAccessor(data)
        interpolatedData.viewRange = 0 to width - axesPadding * 2
        interpolatedData.dataRange = minX to maxX

        var i = 1
        var previousX =
            (interpolatedData[0].time - minX).toFloat() / (maxX - minX) * (width - axesPadding * 2) + axesPadding
        var previousY =
            height - ((interpolatedData[0].value - minY).toFloat() / (maxY - minY).toFloat() * (height - axesPadding * 2) + axesPadding)
        while (i < (width - axesPadding * 2) * 2) {
            val x =
                (interpolatedData.get(i / 2f).time - minX).toFloat() / (maxX - minX) * (width - axesPadding * 2) + axesPadding
            val y =
                height - ((interpolatedData.get(i / 2f).value - minY).toFloat() / (maxY - minY).toFloat() * (height - axesPadding * 2) + axesPadding)

            canvas.drawLine(previousX, previousY, x, y, whitePaint)
            previousX = x
            previousY = y
            i++
        }
    }

    private fun drawTitle(canvas: Canvas) {
        val titleWidth = titleTextPaint.measureText(title)
        canvas.drawText(
            title,
            width / 2f - titleWidth / 2f,
            (axesPadding - titleTextSize) / 2f,
            titleTextPaint
        )
    }

    private fun drawAxes(canvas: Canvas, minX: Int, maxX: Int, minY: Double, maxY: Double) {
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
            val xLabel = "%.0f".format(xPoint)
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

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> downSampledSize = 512
            MotionEvent.ACTION_UP -> {
                downSampledSize = defaultDownSampledSize
                invalidate()
            }
        }
        if (event.pointerCount > 1) {
            mScaleDetector.onTouchEvent(event)
        } else {
            mGestureDetector.onTouchEvent(event)
        }
        return true
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

    private val mGestureListener = object : GestureDetector.SimpleOnGestureListener() {

        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent?,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            val windowWidth = dataRange.max - dataRange.min
            val amount = distanceX / (width) * windowWidth
            Log.d(TAG, "onScroll: DistanceX %.3f".format(distanceX))
            dataRange.min =
                (dataRange.min + amount).coerceAtLeast(0f).coerceAtMost(1f - windowWidth)
            dataRange.max =
                (dataRange.min + windowWidth).coerceAtLeast(windowWidth).coerceAtMost(1f)

            invalidate()
            return true
        }

        override fun onDown(e: MotionEvent?): Boolean {
            return true
        }
    }
    private val mGestureDetector = GestureDetector(context, mGestureListener)

    private val mScaleListener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {

        private var lastSpanX = 0f

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            lastSpanX = detector.currentSpanX
            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            //val factor = 1f / detector.scaleFactor.coerceAtLeast(0f).coerceAtMost(10f)
            var factor =
                (lastSpanX / detector.currentSpanX).coerceAtMost(10f).coerceAtLeast(0.1f)

            factor = 1f - (1f - factor) / 10f

            Log.d(TAG, "onScale: Scale factor %.3f".format(factor))
            var rangeSize = (dataRange.max - dataRange.min)
            val scaleCenter = detector.focusX / width

            val rangeCenter = rangeSize * scaleCenter + dataRange.min
            val newRangeSize = (rangeSize * factor).coerceAtLeast(0.001f).coerceAtMost(1f)

            val sizeL = newRangeSize * rangeCenter
            val sizeR = newRangeSize * (1 - rangeCenter)
            val newMin = (rangeCenter - sizeL).coerceAtLeast(0f)
            val newMax = (rangeCenter + sizeR).coerceAtMost(1f)
            dataRange.min = newMin
            dataRange.max = newMax
            Log.d(TAG, "onScale: new min %.3f new max %.3f".format(dataRange.min, dataRange.max))

            invalidate()
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector?) {
            super.onScaleEnd(detector)

        }
    }
    private val mScaleDetector = ScaleGestureDetector(context, mScaleListener)
}