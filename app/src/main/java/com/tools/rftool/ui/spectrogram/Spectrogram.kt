package com.tools.rftool.ui.spectrogram

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.view.GestureDetectorCompat
import com.tools.rftool.R
import com.tools.rftool.util.graphics.drawFftParams
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class Spectrogram : View {
    companion object {
        private const val TAG = "Spectrogram"

        private const val FREQUENCY_SCROLL_RANGE = 2e6f
        private const val FFT_PARAMS_BOX_HEIGHT = 72
    }

    private val freqMin = context.resources.getInteger(R.integer.tuner_frequency_min)
    private val freqMax = context.resources.getInteger(R.integer.tuner_frequency_max)

    init {
        setWillNotDraw(false)
    }

    interface CenterFrequencyChangeListener {
        fun onCenterFrequencyChange(newFrequency: Int)
    }

    private var _sampleRate = 0
    var sampleRate
        get() = _sampleRate
        set(value) {
            _sampleRate = value
            invalidate()
        }
    private var _centerFrequency: Int = 0
    var centerFrequency: Int
        get() = _centerFrequency
        set(value) {
            _centerFrequency = value
            displayedCenterFrequency = value
            invalidate()
        }

    private var displayedCenterFrequency: Int = 0
    private var centerFrequencyChangeListener: CenterFrequencyChangeListener? = null

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

    private var _adapter: SpectrogramAdapter? = null
    var adapter: SpectrogramAdapter?
        set(value) {
            _adapter = value
            _adapter!!.setAttachedView(this)
        }
        get() = _adapter

    private var lastMax = 100.0
    private var lastMin = 0.0
    val paint = Paint()
    var firstFrame = true

    fun setOnCenterFrequencyChangeListener(listener: CenterFrequencyChangeListener) {
        centerFrequencyChangeListener = listener
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (_adapter != null) {
            val bitmap = _adapter!!.getBitmap()
            canvas.drawBitmap(
                bitmap,
                Rect(0, 0, bitmap.width, bitmap.height),
                Rect(0, 0, width, height),
                paint
            )
            canvas.drawFftParams(sampleRate, displayedCenterFrequency, FFT_PARAMS_BOX_HEIGHT)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (centerFrequencyChangeListener != null) {
            if (event.pointerCount == 1) {
                when(event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        if (event.y > height - FFT_PARAMS_BOX_HEIGHT) {
                            Log.d(TAG, "onTouchEvent: Enabling Frequency scrolling")
                            onGestureListener.isScrolling = true
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        Log.d(TAG, "onTouchEvent: Scrolling terminated")
                        onGestureListener.isScrolling = false
                        if (_centerFrequency != displayedCenterFrequency) {
                            centerFrequencyChangeListener?.onCenterFrequencyChange(
                                displayedCenterFrequency
                            )
                        }
                    }
                }
                return mScrollGestureDetector.onTouchEvent(event)
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        _adapter?.onResize(w, h)
    }

    private val onGestureListener = object : GestureDetector.SimpleOnGestureListener() {
        var isScrolling = false

        override fun onDown(e: MotionEvent?): Boolean {
            return true
        }

        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent?,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            if (isScrolling) {
                Log.d(TAG, "onScroll: Setting new frequency value")
                displayedCenterFrequency = max(
                    freqMin,
                    min(
                        freqMax,
                        (displayedCenterFrequency + (distanceX / width) * FREQUENCY_SCROLL_RANGE).roundToInt()
                    )
                )
                invalidate()
            }
            return true
        }
    }

    private val mScrollGestureDetector = GestureDetectorCompat(context, onGestureListener)
}