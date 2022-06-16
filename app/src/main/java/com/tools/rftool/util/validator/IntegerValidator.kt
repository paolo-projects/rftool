package com.tools.rftool.util.validator

import java.lang.NumberFormatException

class IntegerValidator(defaultValue: Int): Validator() {
    override val DEFAULT_VALUE: String = defaultValue.toString()

    override fun validate(input: String): Boolean {
        try {
            val inputNumber = Integer.parseInt(input)
            return true
        } catch (exc: NumberFormatException) {
        }
        return false
    }
}