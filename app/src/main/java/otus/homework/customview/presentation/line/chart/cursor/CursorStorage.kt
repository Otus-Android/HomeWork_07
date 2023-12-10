package otus.homework.customview.presentation.line.chart.cursor

import android.graphics.PointF
import otus.homework.customview.presentation.line.chart.area.LineAreaStorage

/**
 * Хранилище данных по курсору
 *
 * @param areaStorage хранилище параметров областей `View`
 */
internal class CursorStorage(private val areaStorage: LineAreaStorage) {

    private val point = PointF(UNDEFINED, UNDEFINED)

    /**
     * Обновить данные позиции курсора и вернуть признак выполнения обновления
     *
     * @param x координата курсора по оси X
     * @param y координата курсора по оси Y
     */
    fun update(x: Float, y: Float): Boolean =
        if (areaStorage.chart.contains(x, y)) {
            point.set(x, y)
            true
        } else {
            false
        }

    /** Получить позицию курсора */
    fun getPoint() = point.takeIf { it.x != UNDEFINED && it.y != UNDEFINED }

    /** Очистить данные позиции курсора */
    fun clear() {
        point.set(UNDEFINED, UNDEFINED)
    }

    private companion object {
        const val UNDEFINED = Float.MIN_VALUE
    }
}