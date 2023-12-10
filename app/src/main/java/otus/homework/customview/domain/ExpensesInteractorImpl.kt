package otus.homework.customview.domain

import otus.homework.customview.domain.models.Category
import otus.homework.customview.domain.models.Expense

class ExpensesInteractorImpl(private val repository: ExpensesRepository) : ExpensesInteractor {

    override suspend fun getExpenses(max: Int?, force: Boolean): List<Expense> =
        repository.getExpenses(max, force)

    override suspend fun getCategories(maxExpenses: Int?): List<Category> {
        return getExpenses(maxExpenses).groupBy { it.category }.flatMap { (category, expenses) ->
            listOf(
                Category(
                    category,
                    expenses
                )
            )
        }
    }
}