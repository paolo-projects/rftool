package com.tools.rftool.util.validator

import java.lang.NumberFormatException

class GreaterThanIntegerValidator(private val greaterThan: Int, var defaultValue: Int): Validator() {
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
        return number > greaterThan
    }
}