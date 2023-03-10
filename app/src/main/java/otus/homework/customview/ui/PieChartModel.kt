package otus.homework.customview.ui

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class PieChartModel(internal val items: List<PieChartItem>): Parcelable {

  @IgnoredOnParcel
  val totalAmount = items.sumBy { it.value }

  @IgnoredOnParcel
  internal val sections = mutableListOf<PieChartSection>()

  init {
    var startAngle = 0f
    items.forEach {
      val sweepAngle = it.value / totalAmount.toFloat() * 360
      sections.add(PieChartSection(startAngle, sweepAngle))
      startAngle += sweepAngle
    }
  }
}

@Parcelize
data class PieChartItem(
  val name: String,
  val value: Int
) : Parcelable

internal data class PieChartSection(
  val startAngle: Float,
  val sweepAngle: Float
) {
  val endAngle = startAngle + sweepAngle
}
