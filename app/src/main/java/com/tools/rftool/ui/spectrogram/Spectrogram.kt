package com.tools.rftool.ui.spectrogram

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import com.tools.rftool.util.graphics.drawFftParams

class Spectrogram : View {
    companion object {
        private const val TAG = "Spectrogram"
    }

    init {
        setWillNotDraw(false)
    }

    var sampleRate: Int = 0
    var centerFrequency: Int = 0

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

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (_adapter != null) {
            val bitmap = _adapter!!.getBitmap()
            canvas.drawBitmap(bitmap, Rect(0, 0, bitmap.width, bitmap.height), Rect(0, 0, width, height), paint)
            canvas.drawFftParams(sampleRate, centerFrequency)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        _adapter?.onResize(w, h)
    }
}