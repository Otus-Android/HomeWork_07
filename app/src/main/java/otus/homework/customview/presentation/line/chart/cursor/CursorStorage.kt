package otus.homework.customview.presentation.line.chart.cursor

import android.graphics.PointF
import otus.homework.customview.presentation.line.chart.area.LineAreaProvider

class CursorStorage(private val areaProvider: LineAreaProvider) {

    private val point = PointF(UNDEFINED, UNDEFINED)

    fun getPoint() = point.takeIf { it.x != UNDEFINED && it.y != UNDEFINED }


    fun update(x: Float, y: Float): Boolean =
        if (areaProvider.local.contains(x, y)) {
            point.set(x, y)
            true
        } else {
            false
        }

    fun clear() {
        point.set(UNDEFINED, UNDEFINED)
    }

    private companion object {
        const val UNDEFINED = Float.MIN_VALUE
    }
}