package otus.homework.customview.ui

data class DynamicChartModel(
  internal val lines: List<DynamicChartLine>
)

data class DynamicChartLine(
  val name: String,
  val items: List<DynamicChartItem>
)

data class DynamicChartItem(
  val amount: Int,
  val date: Long
)
