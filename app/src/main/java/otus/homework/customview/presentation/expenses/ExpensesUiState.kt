package otus.homework.customview.presentation.expenses

import otus.homework.customview.domain.Expense

sealed class ExpensesUiState {

    object IDLE : ExpensesUiState()

    object Loading : ExpensesUiState()

    data class Success(val expenses: List<Expense>) : ExpensesUiState()

    data class Error(val message: String) : ExpensesUiState()
}