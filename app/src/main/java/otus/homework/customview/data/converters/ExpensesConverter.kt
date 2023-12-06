package otus.homework.customview.data.converters

import otus.homework.customview.data.ExpenseEntity
import otus.homework.customview.domain.Expense

class ExpensesConverter {

    fun convert(source: ExpenseEntity) = Expense(
        id = source.id,
        name = source.name,
        amount = source.amount,
        category = source.category,
        time = source.time
    )
}