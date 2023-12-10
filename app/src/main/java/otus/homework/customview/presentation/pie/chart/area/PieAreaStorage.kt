package otus.homework.customview.presentation.pie.chart.area

import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import otus.homework.customview.presentation.pie.chart.paints.PiePaints

/**
 * Хранилище параметров областей `View`
 *
 * @param paints параметры рисования графика
 */
internal class PieAreaStorage(private val paints: PiePaints) {

    /** Область всего пространства `View` */
    val global = Rect()

    /** Область пространства `View` с учетом отступов */
    val padding = Rect()

    /** Область графика */
    val chart = RectF()

    /** Область графика в расширенном состоянии (используется для учета `stroke` режима `donut`) */
    val expanded = RectF()

    /** Область графика в стандартном отображении */
    val default = RectF()

    /**
     * Обновить данные областей
     *
     * @param width ширина всего пространства `View`
     * @param height высота всего пространства `View`
     * @param leftPadding левый отступ
     * @param topPadding верхний отступ
     * @param rightPadding правый отступ
     * @param bottomPadding нижний отступ
     */
    fun update(
        width: Int,
        height: Int,
        leftPadding: Int,
        topPadding: Int,
        rightPadding: Int,
        bottomPadding: Int
    ) {
        global.set(0, 0, width, height)

        padding.set(
            global.left + leftPadding,
            global.top + topPadding,
            global.right - rightPadding,
            global.bottom - bottomPadding
        )

        val radius = minOf(padding.height(), padding.width()) / 2f
        val center = PointF(padding.exactCenterX(), padding.exactCenterY())

        chart.set(
            center.x - radius,
            center.y - radius,
            center.x + radius,
            center.y + radius
        )

        updateChart()
    }

    /** Обновить данные [default] и [expanded] областей графика */
    fun updateChart() {
        val pieStroke = paints.pie.strokeWidth / 2f
        expanded.set(
            chart.left + pieStroke,
            chart.top + pieStroke,
            chart.right - pieStroke,
            chart.bottom - pieStroke
        )

        val defaultRadius = chart.width() / 2f * RATIO_DEFAULT_RADIUS

        default.set(
            expanded.left + defaultRadius,
            expanded.top + defaultRadius,
            expanded.right - defaultRadius,
            expanded.bottom - defaultRadius,
        )
    }

    private companion object {

        /** Коэффициент отличия радиуса [default] области от [expanded] */
        const val RATIO_DEFAULT_RADIUS = 0.1F
    }
}