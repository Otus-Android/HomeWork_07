package otus.homework.canvas

data class PieChartSectorData(
    var angle: Float,
    var color: Int = Int.MAX_VALUE,
    val radius: Float = 100F,
    val text: String? = null,
    val category: String,
    var startAngle: Float = 0F,
)
