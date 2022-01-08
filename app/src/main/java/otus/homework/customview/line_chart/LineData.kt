package otus.homework.customview.line_chart

data class LineData(val category: String) {
    val values = mutableListOf<StoreData>()
    var sortedValues: List<StoreData>? = null

    fun add(name: String, amount: Int, time: Long) {
        values.add(StoreData(name, amount, time))
    }
}