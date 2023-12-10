package otus.homework.customview.presentation.pie.chart.paints

import android.content.res.Resources
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import otus.homework.customview.R
import otus.homework.customview.presentation.pie.chart.PieStyle

class PiePaints(private val resources: Resources) {

    /** Настройка отображения области всего пространства */
    val global = Paint().apply {
        color = Color.BLUE
        style = Paint.Style.STROKE
        strokeWidth = resources.getDimension(R.dimen.chart_1)
    }

    /** Настройка отображения области с учетом отсутов */
    val padding = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = resources.getDimension(R.dimen.chart_1)
    }

    /** Настройка отображения области графика */
    val chart = Paint().apply {
        color = Color.GRAY
        style = Paint.Style.STROKE
        strokeWidth = resources.getDimension(R.dimen.chart_1)
    }

    /** Настройка отображения области графика */
    val default = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = resources.getDimension(R.dimen.chart_1)
    }

    /** Настройка отображения отладочной информации по "сетке" */
    val debugGrid = Paint().apply {
        isAntiAlias = true
        color = Color.GRAY
        style = Paint.Style.STROKE
        strokeWidth = resources.getDimension(R.dimen.chart_1)
        pathEffect = DashPathEffect(
            floatArrayOf(
                resources.getDimension(R.dimen.chart_2), resources.getDimension(R.dimen.chart_8)
            ), 0f
        )
    }

    /** Настройка отображения кругового графика */
    val pie = Paint().apply {
        isAntiAlias = true
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = 40f
    }

    /** Настройка отображения подписи, соответствующей позиции курсора */
    val label = Paint().apply {
        color = Color.BLACK
        textSize = resources.getDimension(R.dimen.chart_text_size_24)
    }

    val labelRect: RectF = RectF()

    var style: PieStyle = PieStyle.PIE
        set(value) {
            field = value
            if (field == PieStyle.DONUT) {
                pie.apply {
                    style = Paint.Style.STROKE
                    strokeWidth = resources.getDimension(R.dimen.donut_stroke_width)
                }
            } else {
                pie.apply {
                    style = Paint.Style.FILL
                    strokeWidth = 0f
                }
            }
        }
}