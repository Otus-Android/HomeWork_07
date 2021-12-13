package otus.homework.customview.utils

import android.graphics.Color
import android.graphics.CornerPathEffect
import android.graphics.Paint

private val colors = listOf(
    Color.parseColor("#f06292"),
    Color.parseColor("#ff8a65"),
    Color.parseColor("#9575cd"),
    Color.parseColor("#aab6fe"),
    Color.parseColor("#64b5f6"),
    Color.parseColor("#8bf6ff"),
    Color.parseColor("#4db6ac"),
    Color.parseColor("#81c784"),
    Color.parseColor("#dce775"),
    Color.parseColor("#fff176"),
)

fun createPaint(color: String?, size: Int, forLineChart: Boolean = false): Paint {
    val newPaint = Paint()
    color?.let { colorStr ->
        newPaint.color = Color.parseColor(colorStr)
    } ?: run {
        newPaint.color = colors[size % colors.size]
    }
    newPaint.isAntiAlias = true
    if (forLineChart) {
        newPaint.pathEffect = CornerPathEffect(4f)
        newPaint.style = Paint.Style.STROKE
    }
    return newPaint
}