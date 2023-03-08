package otus.homework.customview.ui

data class PieChartModel(internal val items: List<PieChartItem>) {

  val totalAmount = items.sumBy { it.value }

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

data class PieChartItem(
  val name: String,
  val value: Int
)

internal data class PieChartSection(
  val startAngle: Float,
  val sweepAngle: Float
) {
  val endAngle = startAngle + sweepAngle
}
