package otus.homework.customview.domain

import otus.homework.customview.domain.models.Category
import otus.homework.customview.domain.models.Expense

class ExpensesInteractorImpl(private val repository: ExpensesRepository) : ExpensesInteractor {

    override suspend fun getExpenses(max: Int?, force: Boolean): List<Expense> =
        repository.getExpenses(max, force)

    override suspend fun getCategories(maxExpenses: Int?): List<Category> =
        getExpenses(maxExpenses).groupBy { it.category }.flatMap { (name, expenses) ->
            val sum = expenses.sumOf { it.amount.toLong() }
            listOf(Category(name = name, amount = sum, expenses = expenses))
        }
}