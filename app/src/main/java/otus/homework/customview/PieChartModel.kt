package otus.homework.customview

import android.graphics.Color
import android.graphics.Paint

class PieChartModel(
    var percentToStartAt: Float = 0F,
    var percentOfCircle: Float = 0F,
    var absPercentOfCircle: Float = 0F,
    var colorOfLine: Int = 0,
    var valueModel: BaseValueModel
    ) {

    var paint: Paint

    init {

        if (percentOfCircle < 0 || percentOfCircle > 100) {
            percentOfCircle = 100F
        }

        percentOfCircle = 360 * percentOfCircle / 100

        if (percentToStartAt < 0 || percentToStartAt > 100) {
            percentToStartAt = 0F
        }

        percentToStartAt = 360 * percentToStartAt / 100

        if (absPercentOfCircle < 0 || absPercentOfCircle > 100) {
            absPercentOfCircle = 100F
        }

        absPercentOfCircle = 360 * absPercentOfCircle / 100

        if (colorOfLine == 0) {
            colorOfLine = Color.parseColor("#000000")
        }

        paint = Paint()
        paint.color = colorOfLine
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL_AND_STROKE
        paint.isDither = true

    }

}