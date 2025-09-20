package otus.homework.customview.data

import otus.homework.customview.presentation.PieChartUiInfo

interface UiDataRepo{
    fun getUiData(): PieChartUiInfo
}