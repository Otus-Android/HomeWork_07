package otus.homework.customview.presentation.pie.chart.storages

import android.graphics.PointF
import otus.homework.customview.presentation.pie.chart.models.PieAreaNode
import otus.homework.customview.presentation.pie.chart.utils.MathUtils

/**
 * Хранилище данных по курсору
 *
 * @param areaStorage хранилище параметров областей `View`
 * @param dataStorage хранилище внутренних моделей узлов кругового графика
 */
internal class CursorStorage(
    private val areaStorage: PieAreaStorage,
    private val dataStorage: PieDataStorage
) {

    private var cursorPosition: CursorPosition? = null

    /**
     * Обновить данные позиции курсора и вернуть признак выполнения обновления
     *
     * @param x координата курсора по оси X
     * @param y координата курсора по оси Y
     */
    fun update(x: Float, y: Float): Boolean {
        val localArea = areaStorage.chart
        val radius = localArea.width() / 2f
        val xO = localArea.centerX() - x
        val yO = localArea.centerY() - y
        return if (xO * xO + yO * yO < radius * radius) {
            val angle = MathUtils.calculateTheta(
                centerX = areaStorage.default.centerX(),
                centerY = areaStorage.default.centerY(),
                pointX = x,
                pointY = y
            )
            cursorPosition = CursorPosition(
                PointF(x, y),
                angle,
                dataStorage.getNodeByAngle(angle)
            )
            true
        } else {
            false
        }
    }

    /** Получить внутреннюю модель узла кругового графика, соответствующую позиции курсора */
    fun getNode() = cursorPosition?.node

    /** Очистить данные позиции курсора */
    fun clear() {
        cursorPosition = null
    }

    /**
     * Данные позиции курсора
     *
     * @param point координаты курсора
     * @param angle угол координат курсора на круговой диаграмме
     * @param node внутренняя модель узла кругового графика
     */
    private data class CursorPosition(
        val point: PointF,
        val angle: Float,
        val node: PieAreaNode?
    )
}