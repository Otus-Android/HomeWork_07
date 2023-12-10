package otus.homework.customview.presentation.line.chart.paints

import android.content.res.Resources
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import otus.homework.customview.R
import otus.homework.customview.presentation.line.chart.area.LineAreaStorage

/**
 * Параметры рисования графика
 *
 * @param resources менеджер ресурсов
 * @param areaStorage хранилище параметров областей `View`
 */
internal class LinePaints(
    private val resources: Resources,
    private val areaStorage: LineAreaStorage
) {

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

    val debugTextAxis = Paint().apply {
        isAntiAlias = true
        color = Color.BLACK
        style = Paint.Style.FILL
        textSize = resources.getDimension(R.dimen.chart_text_size_8)
    }

    /** Настройка отображения линий графика */
    val line = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        color = Color.BLUE
        strokeWidth = resources.getDimension(R.dimen.chart_2)
    }

    /** Настройка отображения градиента графика */
    val gradient = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = Color.BLACK
        strokeWidth = resources.getDimension(R.dimen.chart_2)
    }

    /** Настройка отображения курсора */
    val cursor = Paint().apply {
        isAntiAlias = true
        strokeWidth = resources.getDimension(R.dimen.chart_1)
        color = Color.BLACK
    }

    /** Настройка отображения подписи, соответствующей позиции курсора */
    val label = Paint().apply {
        color = Color.BLACK
        textSize = resources.getDimension(R.dimen.chart_text_size_24)
    }

    /** Перерасчитать настройки отображения на основе данных области */
    fun recalculate() {
        gradient.shader = LinearGradient(
            areaStorage.chart.left,
            areaStorage.chart.top,
            areaStorage.chart.left,
            areaStorage.chart.bottom,
            Color.BLUE,
            Color.TRANSPARENT,
            Shader.TileMode.CLAMP
        )
    }
}