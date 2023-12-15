package otus.homework.customview.presentation.expenses

import otus.homework.customview.domain.models.Category

/**
 * Состояние получения данных по расходам
 */
sealed class ExpensesUiState {

    /** Состояние отсутсвия запроса за данными */
    object Idle : ExpensesUiState()

    /** Состояние загрузки данных */
    object Loading : ExpensesUiState()

    /**
     * Состояние успешного получения данных
     *
     * @param categories список категорий расходов
     */
    data class Success(val categories: List<Category>) : ExpensesUiState()

    /**
     * Состояние ошибки получения данных
     *
     * @param message сообщение об ошибке
     */
    data class Error(val message: String) : ExpensesUiState()
}