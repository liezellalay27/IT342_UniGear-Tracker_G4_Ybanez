package com.unigear.tracker.mobile

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast

object UiToast {
    enum class Style {
        SUCCESS,
        ERROR,
        INFO
    }

    fun show(context: Context, message: String, style: Style = Style.INFO, long: Boolean = false) {
        val textView = TextView(context).apply {
            text = message
            setTextColor(getColorForText(style))
            setTypeface(typeface, Typeface.BOLD)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            setPadding(dp(context, 18), dp(context, 14), dp(context, 18), dp(context, 14))
            background = GradientDrawable().apply {
                cornerRadius = dp(context, 18).toFloat()
                setColor(getColorForBackground(style))
                setStroke(dp(context, 1), getColorForStroke(style))
            }
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        Toast(context).apply {
            duration = if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
            setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, dp(context, 96))
            view = textView
            show()
        }
    }

    private fun getColorForBackground(style: Style): Int {
        return when (style) {
            Style.SUCCESS -> 0xFF550000.toInt()
            Style.ERROR -> 0xFF3D0000.toInt()
            Style.INFO -> 0xFF222222.toInt()
        }
    }

    private fun getColorForText(style: Style): Int {
        return when (style) {
            Style.SUCCESS -> 0xFFFFFFFF.toInt()
            Style.ERROR -> 0xFFFFFFFF.toInt()
            Style.INFO -> 0xFFFFFFFF.toInt()
        }
    }

    private fun getColorForStroke(style: Style): Int {
        return when (style) {
            Style.SUCCESS -> 0xFFEFBF04.toInt()
            Style.ERROR -> 0xFFF5D76E.toInt()
            Style.INFO -> 0xFF555555.toInt()
        }
    }

    private fun dp(context: Context, value: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }
}