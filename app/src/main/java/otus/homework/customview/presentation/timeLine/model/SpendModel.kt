package otus.homework.customview.presentation.timeLine.model

import otus.homework.customview.domain.Category

data class SpendModel(
    val name: String,
    val amount: Int,
    val category: Category,
    val date: String,
    val part: Pair<Float, Float>,
)
