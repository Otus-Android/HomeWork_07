package otus.homework.customview.presentation.line.chart.area

import android.graphics.Rect
import android.graphics.RectF

class LineAreaProvider {

    /** Область всего пространства `View` */
    val global = Rect()

    /** Область пространства `View` с учетом отступов */
    val padding = Rect()

    /** Область графика */
    val chart = RectF()

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

        chart.set(padding)
    }
}
