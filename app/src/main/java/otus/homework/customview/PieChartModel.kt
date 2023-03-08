package otus.homework.customview

data class PieChartModel(val items: List<PieChartItem>) {
  val totalAmount = items.sumByDouble { it.value.toDouble() }

  fun getRatioByIndex(index: Int): Float {
    if (index !in items.indices) return 0f
    return items[index].value.toFloat() / totalAmount.toFloat()
  }
}

data class PieChartItem(
  val name: String,
  val value: Number
)
