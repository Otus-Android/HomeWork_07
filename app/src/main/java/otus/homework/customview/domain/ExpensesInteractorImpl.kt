package otus.homework.customview.domain

class ExpensesInteractorImpl(private val repository: ExpensesRepository) : ExpensesInteractor {

    override suspend fun getExpenses() = repository.getExpenses()
}