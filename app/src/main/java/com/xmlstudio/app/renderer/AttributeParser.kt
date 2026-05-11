package com.xmlstudio.app.renderer

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup

object AttributeParser {

    fun parseDimension(context: Context, value: String, defaultDp: Float = 0f): Int {
        if (value.isBlank()) return dpToPx(context, defaultDp)
        return when {
            value.endsWith("dp") -> {
                val num = value.removeSuffix("dp").toFloatOrNull() ?: defaultDp
                dpToPx(context, num)
            }
            value.endsWith("sp") -> {
                val num = value.removeSuffix("sp").toFloatOrNull() ?: defaultDp
                spToPx(context, num)
            }
            value.endsWith("px") -> {
                value.removeSuffix("px").toIntOrNull() ?: dpToPx(context, defaultDp)
            }
            value == "match_parent" || value == "fill_parent" -> ViewGroup.LayoutParams.MATCH_PARENT
            value == "wrap_content" -> ViewGroup.LayoutParams.WRAP_CONTENT
            else -> value.toIntOrNull() ?: dpToPx(context, defaultDp)
        }
    }

    fun parseTextSize(context: Context, value: String, defaultSp: Float = 14f): Float {
        return when {
            value.endsWith("sp") -> value.removeSuffix("sp").toFloatOrNull() ?: defaultSp
            value.endsWith("dp") -> value.removeSuffix("dp").toFloatOrNull() ?: defaultSp
            else -> value.toFloatOrNull() ?: defaultSp
        }
    }

    fun parseColor(value: String, default: Int = Color.BLACK): Int {
        return try {
            Color.parseColor(if (value.startsWith("#")) value else "#$value")
        } catch (e: Exception) {
            default
        }
    }

    fun parseGravity(value: String): Int {
        var gravity = Gravity.NO_GRAVITY
        val parts = value.split("|")
        parts.forEach { part ->
            gravity = gravity or when (part.trim().lowercase()) {
                "center" -> Gravity.CENTER
                "center_horizontal" -> Gravity.CENTER_HORIZONTAL
                "center_vertical" -> Gravity.CENTER_VERTICAL
                "top" -> Gravity.TOP
                "bottom" -> Gravity.BOTTOM
                "start", "left" -> Gravity.START
                "end", "right" -> Gravity.END
                else -> Gravity.NO_GRAVITY
            }
        }
        return gravity
    }

    fun parseLayoutSize(value: String): Int = when (value) {
        "match_parent", "fill_parent" -> ViewGroup.LayoutParams.MATCH_PARENT
        "wrap_content" -> ViewGroup.LayoutParams.WRAP_CONTENT
        else -> value.toIntOrNull() ?: ViewGroup.LayoutParams.WRAP_CONTENT
    }

    private fun dpToPx(context: Context, dp: Float): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics).toInt()

    private fun spToPx(context: Context, sp: Float): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.resources.displayMetrics).toInt()
}
