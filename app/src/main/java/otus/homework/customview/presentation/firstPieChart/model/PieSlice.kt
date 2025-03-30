package otus.homework.customview.presentation.firstPieChart.model

import android.graphics.Paint
import android.graphics.PointF

data class PieSlice(
    val categoryTitle: String,
    val amount: Int,
    var startAngle: Float,
    var sweepAngle: Float,
    val indicatorCircleLocation: PointF,
    val paint: Paint,
)
