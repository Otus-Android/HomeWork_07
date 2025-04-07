package otus.homework.customview.presentation.timeLine.model

import otus.homework.customview.domain.Category

data class TimeLineData(
    val selected: Category,
    val availableCategories: Set<Category>,
    val spends: Map<Category, List<SpendModel>>,
    val totalDays: Set<String>,
    val maxAmount: Int
)
