package com.tools.rftool.util.validator

abstract class Validator {
    abstract val DEFAULT_VALUE: String
    abstract fun validate(input: String): Boolean
}