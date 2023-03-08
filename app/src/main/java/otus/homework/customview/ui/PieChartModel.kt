package otus.homework.customview.ui

data class PieChartModel(internal val items: List<PieChartItem>) {

  val totalAmount = items.sumBy { it.value }

  private val _sections = mutableListOf<Section>()
  internal val sections get() = _sections.toList()

  init {
    var startAngle = 0f
    items.forEach {
      val sweepAngle = it.value / totalAmount.toFloat() * 360
      _sections.add(Section(startAngle, sweepAngle))
      startAngle += sweepAngle
    }
  }
}

data class PieChartItem(
  val name: String,
  val value: Int
)

internal data class Section(
  val startAngle: Float,
  val sweepAngle: Float
) {
  val endAngle = startAngle + sweepAngle
}
