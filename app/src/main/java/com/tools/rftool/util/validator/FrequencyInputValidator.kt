package com.tools.rftool.util.validator

import java.lang.NumberFormatException

class FrequencyInputValidator(defaultValue: Int): Validator() {
    override val DEFAULT_VALUE = defaultValue.toString()

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
        return number in 24000000..1766000000
    }
}