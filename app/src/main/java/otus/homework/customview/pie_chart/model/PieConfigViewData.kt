package otus.homework.customview.pie_chart.model

import otus.homework.customview.model.Category

class PieConfigViewData(
    val payload: List<Category>,
    val clickListener: (categoryName: String?) -> Unit = {},
    val minSizePx: Int = 200,
    val innerSizePercent: Float = 0.7f,
    val animatedSizePercent: Float = 1.2f,
)