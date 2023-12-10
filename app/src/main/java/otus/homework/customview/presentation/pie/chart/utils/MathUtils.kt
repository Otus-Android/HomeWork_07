package otus.homework.customview.presentation.pie.chart.utils

import kotlin.math.atan2

/**
 * Вспомогательные матетатические средства
 */
object MathUtils {

    /**
     * Вычислить угол полярных координат
     *
     * @param centerX координата центра по оси X
     * @param centerY координата центра по оси Y
     * @param pointX координата точки по оси X
     * @param pointY координата точки по оси Y
     */
    fun calculateTheta(centerX: Float, centerY: Float, pointX: Float, pointY: Float): Float {
        val xDistance = pointX - centerX
        val yDistance = pointY - centerY
        val theta = atan2(-yDistance, xDistance).toDouble()
        val angle = Math.toDegrees(theta).toFloat()
        return if (angle < 0) -angle else 360 - angle
    }
}