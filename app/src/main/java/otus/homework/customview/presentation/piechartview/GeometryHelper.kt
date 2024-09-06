package otus.homework.customview.presentation.piechartview

import android.graphics.RectF
import android.view.MotionEvent
import otus.homework.customview.presentation.model.PieChartAngle
import otus.homework.customview.utils.DEGREES_IN_CIRCLE
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

object GeometryHelper {

    fun isClickInsidePieChart(event: MotionEvent, pieChartContainer : RectF): Boolean {
        val x = event.x
        val y = event.y

        val centerX = pieChartContainer.centerX()
        val centerY = pieChartContainer.centerY()
        val radius = pieChartContainer.width() / 2

        val distance = sqrt((x - centerX).toDouble().pow(2.0) + (y - centerY).toDouble().pow(2.0))

        return distance <= radius
    }

    fun isClickWithinPieChartAngle(event: MotionEvent, angle: PieChartAngle, pieChartContainer : RectF): Boolean {
        val x = event.x
        val y = event.y

        val centerX = pieChartContainer.centerX()
        val centerY = pieChartContainer.centerY()

        // Angle of the click relative to the center of the pie chart
        val clickAngle = Math.toDegrees(atan2((y - centerY).toDouble(), (x - centerX).toDouble())).toFloat()

        val normalizedClickAngle = (clickAngle + DEGREES_IN_CIRCLE) % DEGREES_IN_CIRCLE
        return normalizedClickAngle >= angle.startAngle && normalizedClickAngle <= angle.endAngle
    }

}