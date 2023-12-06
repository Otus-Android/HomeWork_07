package otus.homework.customview.presentation.line.chart.models

import android.content.res.Resources
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import otus.homework.customview.R

class LinePaints(private val resources: Resources) {

    val global = Paint().apply {
        color = Color.BLUE
        style = Paint.Style.STROKE
        strokeWidth = resources.getDimension(R.dimen.chart_1)
    }

    val padding = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = resources.getDimension(R.dimen.chart_1)
    }

    val local = Paint().apply {
        color = Color.GRAY
        style = Paint.Style.STROKE
        strokeWidth = resources.getDimension(R.dimen.chart_1)
    }

    val grid = Paint().apply {
        isAntiAlias = true
        color = Color.GRAY
        style = Paint.Style.STROKE
        strokeWidth = resources.getDimension(R.dimen.chart_1)
        pathEffect = DashPathEffect(
            floatArrayOf(
                resources.getDimension(R.dimen.chart_2),
                resources.getDimension(R.dimen.chart_8)
            ),
            0f
        )
    }

    val textAxis = Paint().apply {
        isAntiAlias = true
        color = Color.BLACK
        style = Paint.Style.FILL
        textSize = resources.getDimension(R.dimen.chart_text_size_8)
    }

    val gradient = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = Color.BLACK
        strokeWidth = resources.getDimension(R.dimen.chart_2)
    }

    val line = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        color = Color.BLUE
        strokeWidth = resources.getDimension(R.dimen.chart_2)
    }

    val currentLine = Paint().apply {
        isAntiAlias = true
        strokeWidth = resources.getDimension(R.dimen.chart_2)
        color = Color.BLACK
    }

    val text = Paint().apply {
        color = Color.BLACK
        textSize = resources.getDimension(R.dimen.chart_text_size_24)
    }
}