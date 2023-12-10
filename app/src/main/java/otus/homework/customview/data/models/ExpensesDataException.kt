package otus.homework.customview.data.models


/**
 * Исключение получения данных по расходам `data` слоя
 *
 * @param cause причина получения исключения
 */
class ExpensesDataException(cause: Exception) : Exception(cause)