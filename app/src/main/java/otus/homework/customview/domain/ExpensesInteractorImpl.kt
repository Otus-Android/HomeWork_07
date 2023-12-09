package otus.homework.customview.domain

class ExpensesInteractorImpl(private val repository: ExpensesRepository) : ExpensesInteractor {

    override suspend fun getExpenses(max: Int?): List<Expense> = repository.getExpenses(max)
}