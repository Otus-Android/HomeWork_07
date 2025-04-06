package otus.homework.customview.presentation.timeLine.model

import otus.homework.customview.domain.Category
import otus.homework.customview.domain.Spend

data class TimeLineData(
    val selected: Category,
    val available: Set<Category>,
    val spends: List<Spend>,
)
