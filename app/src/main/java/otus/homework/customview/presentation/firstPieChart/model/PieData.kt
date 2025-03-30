package otus.homework.customview.presentation.firstPieChart.model

data class PieData(
    val totalValue: Int = 0,
    val pieSlices: Map<String, PieSlice>,
    val title: String = "Total:",
)
