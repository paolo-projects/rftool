package com.tools.rftool.util.validator

import android.content.Context
import com.tools.rftool.R
import java.lang.NumberFormatException

class FrequencyInputValidator(private val context: Context, var defaultValue: Int): Validator() {
    override val DEFAULT_VALUE = defaultValue.toString()
    private val FREQ_MIN = context.resources.getInteger(R.integer.tuner_frequency_min)
    private val FREQ_MAX = context.resources.getInteger(R.integer.tuner_frequency_max)

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
        return number in FREQ_MIN..FREQ_MAX
    }
}