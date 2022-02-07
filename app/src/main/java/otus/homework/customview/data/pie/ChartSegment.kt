package otus.homework.customview.data.pie

import android.graphics.Paint
import android.graphics.PointF
import kotlinx.parcelize.RawValue

data class ChartSegment(
  val title: String,
  var data: Double,
  var startAngle: Float,
  var endAngle: Float,
  var markerPosition: PointF,
  val paint: @RawValue Paint
)