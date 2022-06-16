package com.tools.rftool.spectrogram

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import androidx.collection.CircularArray
import androidx.core.graphics.set
import com.tools.rftool.rtlsdr.IQ
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

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