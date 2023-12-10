package otus.homework.customview.data.converters

import otus.homework.customview.data.models.ExpenseEntity
import otus.homework.customview.domain.models.Expense

/**
 * Конвертер данных по расходам
 */
class ExpensesConverter {

    /** Преобразовать [ExpenseEntity] в [Expense] */
    fun convert(source: ExpenseEntity) = Expense(
        id = source.id,
        name = source.name,
        amount = source.amount,
        category = source.category,
        time = source.time
    )
}