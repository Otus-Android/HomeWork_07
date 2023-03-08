package otus.homework.customview

data class PieChartModel(val items: List<PieChartDto>) {
  private val totalAmount = items.sumBy { it.amount }

  fun getRatioByIndex(index: Int): Float {
    if (index !in items.indices) return 0f
    return items[index].amount.toFloat() / totalAmount.toFloat()
  }
}
