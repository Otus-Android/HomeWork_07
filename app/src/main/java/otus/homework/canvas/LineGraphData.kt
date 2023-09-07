package otus.homework.canvas

data class LineGraphData(
    val valueX: Float,
    val valueY: Float,
    val xToString: (Float) -> String = {v -> "$v"}
)
