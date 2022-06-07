package otus.homework.customview

sealed class PieChartState {
    object Idle : PieChartState()

    data class Init(
        val pieChartItems: List<PieChartItem>
    ) : PieChartState() {
        val maxConnection get() = pieChartItems.sumBy { it.amount }
    }

    data class DrawComplete(
        val piePoints: List<PieChartCategoryPoint>
    ) : PieChartState()
}
