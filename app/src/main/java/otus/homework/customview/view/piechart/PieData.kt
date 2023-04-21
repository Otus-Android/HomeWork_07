package otus.homework.customview.view.piechart

class PieData {
    var paintIndex = 0
    var pieSlices = HashMap<String, PieSlice>()
    var totalValue = 0.0

    fun add(name: String, value: Float) {
        if (pieSlices.containsKey(name)) {
            pieSlices[name]?.let { it.value += value }
        } else {
            pieSlices[name] = PieSlice(name, value, 0f, 0f, paintIndex)
            paintIndex++
        }
        totalValue += value
    }
}