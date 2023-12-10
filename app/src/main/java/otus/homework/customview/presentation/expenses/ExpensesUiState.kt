package otus.homework.customview.presentation.expenses

import otus.homework.customview.domain.models.Category

sealed class ExpensesUiState {

    object IDLE : ExpensesUiState()

    object Loading : ExpensesUiState()

    data class Success(val categories: List<Category>) : ExpensesUiState()

    data class Error(val message: String) : ExpensesUiState()
}