package otus.homework.customview.presentation.timeLine.model

import otus.homework.customview.domain.Category

data class TimeLineData(
    val selected: Category,
    val available: Set<Category>,
    val spends: List<SpendModel>,
    val totalDays: Int,
    val maxAmount: Int
)
