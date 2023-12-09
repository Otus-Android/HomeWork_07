package otus.homework.customview.presentation.line.chart.area

import android.graphics.Rect
import android.graphics.RectF

class LineAreaProvider() {

    /** Область всего пространства */
    val global = Rect()

    /** Область с учетом отступов */
    val padding = Rect()

    /** Область, на которой рисуется сам график */
    val local = RectF()

    fun update(
        leftPosition: Int,
        topPosition: Int,
        rightPosition: Int,
        bottomPosition: Int,
        leftPadding: Int,
        topPadding: Int,
        rightPadding: Int,
        bottomPadding: Int
    ) {
        global.set(leftPosition, topPosition, rightPosition, bottomPosition)

        padding.set(
            global.left + leftPadding,
            global.top + topPadding,
            global.right - rightPadding,
            global.bottom - bottomPadding
        )

        local.set(padding)
    }
}
