package com.tools.rftool.util.validator

import android.content.Context
import com.tools.rftool.R
import java.lang.NumberFormatException

class SampleRateInputValidator(private val context: Context, var defaultValue: Int) : Validator() {
    override val DEFAULT_VALUE = defaultValue.toString()
    private val SAMPLERATE_RANGE1_MIN = context.resources.getInteger(R.integer.sample_rate_range1_min)
    private val SAMPLERATE_RANGE1_MAX = context.resources.getInteger(R.integer.sample_rate_range1_max)
    private val SAMPLERATE_RANGE2_MIN = context.resources.getInteger(R.integer.sample_rate_range2_min)
    private val SAMPLERATE_RANGE2_MAX = context.resources.getInteger(R.integer.sample_rate_range2_max)

    override fun validate(input: String): Boolean {
        try {
            val inputNumber = Integer.parseInt(input)
            if (isInRange(inputNumber)) {
                return true
            }
        } catch (exc: NumberFormatException) {
        }
        return false
    }

    private fun isInRange(number: Int): Boolean {
        return number in SAMPLERATE_RANGE1_MIN..SAMPLERATE_RANGE1_MAX
                || number in SAMPLERATE_RANGE2_MIN..SAMPLERATE_RANGE2_MAX
    }
}