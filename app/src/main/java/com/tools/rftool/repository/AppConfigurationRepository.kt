package com.tools.rftool.repository

import android.content.Context
import com.tools.rftool.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class AppConfigurationRepository @Inject constructor(@ApplicationContext private val context: Context) {
    companion object {
        private const val PREFS_KEY = "rftool_rf_preferences"
        private const val PREFS_SAMPLE_RATE = "rf_sample_rate"
        private const val PREFS_CENTER_FREQUENCY = "rf_center_frequency"
        private const val PREFS_GAIN = "rf_gain"
        private const val PREFS_PPM_ERROR = "rf_ppm_error"
        private const val PREFS_COLOR_MAP = "rf_color_map"
        private const val PREFS_AUTO_RECORD_ENABLED = "rf_auto_rec_enabled"
        private const val PREFS_AUTO_RECORD_THRESHOLD = "rf_auto_rec_threshold"
        private const val PREFS_AUTO_RECORD_TIME = "rf_auto_rec_time"
    }

    private val sharedPreferences = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
    private val defaultSampleRate = context.resources.getInteger(R.integer.default_sample_rate)
    private val defaultCenterFrequency =
        context.resources.getInteger(R.integer.default_center_frequency)
    private val defaultGain = context.resources.getInteger(R.integer.default_gain)
    private val defaultPpmError = context.resources.getInteger(R.integer.default_ppm_error)
    private val defaultColorMap = 0
    private val defaultAutoRecEnabled = false
    private val defaultAutoRecThreshold = 50f
    private val defaultAutoRecTimeMs = 1000

    private val dispatcher = Dispatchers.Main

    private val _sampleRate =
        MutableStateFlow(sharedPreferences.getInt(PREFS_SAMPLE_RATE, defaultSampleRate))
    private val _centerFrequency =
        MutableStateFlow(sharedPreferences.getInt(PREFS_CENTER_FREQUENCY, defaultCenterFrequency))
    private val _gain = MutableStateFlow(sharedPreferences.getInt(PREFS_GAIN, defaultGain))
    private val _ppmError =
        MutableStateFlow(sharedPreferences.getInt(PREFS_PPM_ERROR, defaultPpmError))
    private val _colorMap =
        MutableStateFlow(sharedPreferences.getInt(PREFS_COLOR_MAP, defaultColorMap))
    private val _autoRecEnabled = MutableStateFlow(
        sharedPreferences.getBoolean(
            PREFS_AUTO_RECORD_ENABLED, defaultAutoRecEnabled
        )
    )
    private val _autoRecThreshold = MutableStateFlow(
        sharedPreferences.getFloat(
            PREFS_AUTO_RECORD_THRESHOLD, defaultAutoRecThreshold
        )
    )
    private val _autoRecTimeMs =
        MutableStateFlow(sharedPreferences.getInt(PREFS_AUTO_RECORD_TIME, defaultAutoRecTimeMs))

    val sampleRate = _sampleRate.asStateFlow()
    val centerFrequency = _centerFrequency.asStateFlow()
    val gain = _gain.asStateFlow()
    val ppmError = _ppmError.asStateFlow()
    val colorMap = _colorMap.asStateFlow()
    val autoRecEnabled = _autoRecEnabled.asStateFlow()
    val autoRecThreshold = _autoRecThreshold.asStateFlow()
    val autoRecTimeMs = _autoRecTimeMs.asStateFlow()

    fun setSampleRate(value: Int) {
        CoroutineScope(dispatcher).launch {
            sharedPreferences.edit().putInt(PREFS_SAMPLE_RATE, value).commit()
            _sampleRate.emit(value)
        }
    }

    fun setCenterFrequency(value: Int) {
        CoroutineScope(dispatcher).launch {
            sharedPreferences.edit().putInt(PREFS_CENTER_FREQUENCY, value).commit()
            _centerFrequency.emit(value)
        }
    }

    fun setGain(value: Int) {
        CoroutineScope(dispatcher).launch {
            sharedPreferences.edit().putInt(PREFS_GAIN, value).commit()
            _gain.emit(value)
        }
    }

    fun setPpmError(value: Int) {
        CoroutineScope(dispatcher).launch {
            sharedPreferences.edit().putInt(PREFS_PPM_ERROR, value).commit()
            _ppmError.emit(value)
        }
    }

    fun setColorMap(value: Int) {
        CoroutineScope(dispatcher).launch {
            sharedPreferences.edit().putInt(PREFS_COLOR_MAP, value).commit()
            _colorMap.emit(value)
        }
    }

    fun setAutoRecEnabled(value: Boolean) {
        CoroutineScope(dispatcher).launch {
            sharedPreferences.edit().putBoolean(PREFS_AUTO_RECORD_ENABLED, value).commit()
            _autoRecEnabled.emit(value)
        }
    }

    fun setAutoRecThreshold(value: Float) {
        CoroutineScope(dispatcher).launch {
            sharedPreferences.edit().putFloat(PREFS_AUTO_RECORD_THRESHOLD, value).commit()
            _autoRecThreshold.emit(value)
        }
    }

    fun setAutoRecTimeMs(value: Int) {
        CoroutineScope(dispatcher).launch {
            sharedPreferences.edit().putInt(PREFS_AUTO_RECORD_TIME, value).commit()
            _autoRecTimeMs.emit(value)
        }
    }
}