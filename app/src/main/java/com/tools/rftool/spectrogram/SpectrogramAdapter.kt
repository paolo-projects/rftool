package com.tools.rftool.spectrogram

import android.graphics.Bitmap
import android.view.View
import com.tools.rftool.rtlsdr.IQ

abstract class SpectrogramAdapter {
    private var attachedView: View? = null
    open fun onResize(width: Int, height: Int) {
    }
    abstract fun getBitmap(): Bitmap
    fun setAttachedView(view: View) {
        attachedView = view
    }
    fun notifyDataChanged() {
        attachedView?.invalidate()
    }
}