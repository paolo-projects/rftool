package com.tools.rftool.ui.animation

import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.Button
import com.tools.rftool.R
import kotlinx.coroutines.delay

class ButtonAnimation(
    private val button: Button,
    private val transitionDuration: Long = 150,
    private val pauseDuration: Long = 500
) {
    private val originalBg: Int

    private val successColor = button.context.getColor(R.color.success)
    private val errorColor = button.context.getColor(R.color.error)

    private var ongoingAnimation: AnimatorSet? = null

    init {
        val primaryColor = intArrayOf(com.google.android.material.R.attr.colorPrimary)
        val array = button.context.obtainStyledAttributes(primaryColor)
        originalBg = array.getColor(0, Color.TRANSPARENT)
        array.recycle()
    }

    fun animateSuccess() {
        ongoingAnimation?.removeAllListeners()
        ongoingAnimation?.end()
        ongoingAnimation?.cancel()

        ongoingAnimation = AnimatorSet().apply {
            play(ValueAnimator.ofArgb(originalBg, successColor).apply {
                duration = transitionDuration
                addUpdateListener {
                    button.setBackgroundColor(it.animatedValue as Int)
                }
            })
            play(ValueAnimator.ofArgb(successColor, originalBg).apply {
                duration = transitionDuration
                addUpdateListener {
                    button.setBackgroundColor(it.animatedValue as Int)
                }
            }).after(pauseDuration)
        }

        ongoingAnimation!!.start()
    }

    fun animateError() {
        ongoingAnimation?.removeAllListeners()
        ongoingAnimation?.end()
        ongoingAnimation?.cancel()

        ongoingAnimation = AnimatorSet().apply {
            play(ValueAnimator.ofArgb(originalBg, errorColor).apply {
                duration = transitionDuration
                addUpdateListener {
                    button.setBackgroundColor(it.animatedValue as Int)
                }
            })
            play(ValueAnimator.ofArgb(errorColor, originalBg).apply {
                duration = transitionDuration
                addUpdateListener {
                    button.setBackgroundColor(it.animatedValue as Int)
                }
            }).after(pauseDuration)
        }

        ongoingAnimation!!.start()
    }
}