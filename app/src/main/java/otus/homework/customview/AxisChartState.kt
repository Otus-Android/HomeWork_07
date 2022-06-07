package otus.homework.customview

sealed class AxisChartState {
    object Idle : AxisChartState()

    data class Init(
        val chartItems: List<PieChartItem>
    ) : AxisChartState() {
        val maxAmount get() = chartItems.maxOf { it.amount }.toFloat()
    }

    data class DrawComplete(
        val categoryItems: List<AxisChartCategoryItem>,
        val maxAmount: Float
    ) : AxisChartState()
}
