package otus.homework.customview.presentation.line

import otus.homework.customview.domain.Expense

sealed class LineChartUiState {

    object IDLE : LineChartUiState()

    object Loading : LineChartUiState()

    data class Success(val expenses: List<Expense>) : LineChartUiState()

    data class Error(val message: String) : LineChartUiState()
}
