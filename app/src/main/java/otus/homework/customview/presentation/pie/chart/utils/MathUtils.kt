package otus.homework.customview.presentation.pie.chart.utils

import kotlin.math.atan2

object MathUtils {

    fun calculateThetaV2(centerX: Float, centerY: Float, x: Float, y: Float): Float {
        val xDistance = x - centerX.toDouble()
        val yDistance = y - centerY.toDouble()
        var angle = Math.toDegrees(atan2(-yDistance, xDistance))
        if (angle < 0) {
            angle = -angle
        } else {
            angle = 360 - angle
        }
        return angle.toFloat()
    }
}