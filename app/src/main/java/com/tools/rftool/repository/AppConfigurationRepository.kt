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
    private val defaultCenterFrequency = context.resources.getInteger(R.integer.default_center_frequency)
    private val defaultGain = context.resources.getInteger(R.integer.default_gain)
    private val defaultPpmError = context.resources.getInteger(R.integer.default_ppm_error)
    private val defaultColorMap = 0
    private val defaultAutoRecEnabled = false
    private val defaultAutoRecThreshold = 50f
    private val defaultAutoRecTimeMs = 1000

    private val dispatcher = Dispatchers.IO

    var sampleRate: Int
        get() = sharedPreferences.getInt(PREFS_SAMPLE_RATE, defaultSampleRate)
        set(value) = sharedPreferences.edit().putInt(PREFS_SAMPLE_RATE, value).apply()

    var centerFrequency: Int
        get() = sharedPreferences.getInt(PREFS_CENTER_FREQUENCY, defaultCenterFrequency)
        set(value) = sharedPreferences.edit().putInt(PREFS_CENTER_FREQUENCY, value).apply()

    var gain: Int
        get() = sharedPreferences.getInt(PREFS_GAIN, defaultGain)
        set(value) = sharedPreferences.edit().putInt(PREFS_GAIN, value).apply()

    var ppmError: Int
        get() = sharedPreferences.getInt(PREFS_PPM_ERROR, defaultPpmError)
        set(value) = sharedPreferences.edit().putInt(PREFS_PPM_ERROR, value).apply()

    var colorMap: Int
        get() = sharedPreferences.getInt(PREFS_COLOR_MAP, defaultColorMap)
        set(value) = sharedPreferences.edit().putInt(PREFS_COLOR_MAP, value).apply()

    var autoRecEnabled: Boolean
        get() = sharedPreferences.getBoolean(PREFS_AUTO_RECORD_ENABLED, defaultAutoRecEnabled)
        set(value) = sharedPreferences.edit().putBoolean(PREFS_AUTO_RECORD_ENABLED, value).apply()

    var autoRecThreshold: Float
        get() = sharedPreferences.getFloat(PREFS_AUTO_RECORD_THRESHOLD, defaultAutoRecThreshold)
        set(value) {
            sharedPreferences.edit().putFloat(PREFS_AUTO_RECORD_THRESHOLD, value).apply()
            CoroutineScope(dispatcher + Job()).launch {
                _autoRecThresholdFlow.emit(value)
            }
        }

    var autoRecTimeMs: Int
        get() = sharedPreferences.getInt(PREFS_AUTO_RECORD_TIME, defaultAutoRecTimeMs)
        set(value) = sharedPreferences.edit().putInt(PREFS_AUTO_RECORD_TIME, value).apply()

    private val _autoRecThresholdFlow = MutableStateFlow(autoRecThreshold)
    val autoRecThresholdFlow = _autoRecThresholdFlow.asStateFlow()
}