package otus.homework.customview.presentation.pie

import otus.homework.customview.domain.Expense
import otus.homework.customview.presentation.pie.chart.models.PieData

sealed class PieChartUiState {

    object IDLE : PieChartUiState()

    object Loading : PieChartUiState()

    data class Success(val expenses: PieData<Float>) : PieChartUiState()

    data class Error(val message: String) : PieChartUiState()
}
