package otus.homework.customview.lineChart

class LineChartStringDecorator(
    val decorateX: (Int) -> String,
    val decorateY: (Int) -> String
) {
    companion object {
        fun default() = LineChartStringDecorator(
            decorateX = { it.toString() },
            decorateY = { it.toString() }
        )
    }
}