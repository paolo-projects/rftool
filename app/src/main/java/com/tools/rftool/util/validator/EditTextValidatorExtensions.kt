package com.tools.rftool.util.validator

import android.view.View
import android.widget.EditText

fun EditText.setFocusLostValidator(validator: Validator) {
    this.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
        if (!hasFocus && !validator.validate(this.text.toString())) {
            this.setText(validator.DEFAULT_VALUE)
        }
    }
}