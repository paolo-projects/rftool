package com.tools.rftool.ui.spectrogram

import android.graphics.Bitmap
import android.util.Log

class FftTimeSeriesSpectrogramAdapter() : SpectrogramAdapter() {

    companion object {
        private const val TAG = "FftTimeSeriesSpectrogramAdapter"
    }

    private var _bitmap: Bitmap = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888)

    override fun getBitmap(): Bitmap {
        return _bitmap
    }

    fun setBitmap(bitmap: Bitmap) {
        Log.d(TAG, "addDataEntry: adding new data to adapter")
        _bitmap = bitmap
        notifyDataChanged()
    }
}