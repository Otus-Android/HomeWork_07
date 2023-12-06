package otus.homework.customview.presentation.pie

import otus.homework.customview.domain.Expense

sealed class PieChartUiState {

    object IDLE : PieChartUiState()

    object Loading : PieChartUiState()

    data class Success(val expenses: List<Expense>) : PieChartUiState()

    data class Error(val message: String) : PieChartUiState()
}
