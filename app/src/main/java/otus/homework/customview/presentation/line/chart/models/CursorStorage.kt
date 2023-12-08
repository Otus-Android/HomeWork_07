package otus.homework.customview.presentation.line.chart.models

import android.graphics.PointF

class CursorStorage(private val areaProvider: LineAreaProvider) {

    private val point = PointF(UNDEFINED, UNDEFINED)

    fun getCurrentLineX() = point.x.takeIf { it != UNDEFINED }

    fun getPoint() = point.takeIf { it.x != UNDEFINED && it.y != UNDEFINED }


    fun update(x: Float, y: Float): Boolean =
        if (areaProvider.local.contains(x, y)) {
            point.set(x, y)
            true
        } else {
            false
        }

    fun clearCurrentLineX() {
        point.x = UNDEFINED
    }

    private companion object {
        const val UNDEFINED = Float.MIN_VALUE
    }
}