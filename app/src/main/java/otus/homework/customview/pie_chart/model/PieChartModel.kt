package otus.homework.customview.pie_chart.model

import otus.homework.customview.model.Category

class PieChartModel(
    var innerRadius: Float = 0f,
    var outerRadius: Float = 0f,
    var animateTo: Float = 0f,
    var inputData: List<Category> = emptyList(),
    var drawData: List<CategoryArc> = emptyList(),
    var minSizePx: Int = 200,
    var innerSizePercent: Float = 0.7f,
    var animatedSizePercent: Float = 1.2f,
) {

    fun config(
        minSizePx: Int = 200,
        innerSizePercent: Float = 0.7f,
        animatedSizePercent: Float = 1.2f,
    ) {
        this.minSizePx = minSizePx
        this.innerSizePercent = innerSizePercent
        this.animatedSizePercent = animatedSizePercent
    }

}