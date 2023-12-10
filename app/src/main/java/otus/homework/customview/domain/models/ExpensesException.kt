package otus.homework.customview.domain.models

/**
 * Исключение получения данных по расходам
 *
 * @param cause причина получения исключения
 */
class ExpensesException(cause: Exception) : Exception(cause)