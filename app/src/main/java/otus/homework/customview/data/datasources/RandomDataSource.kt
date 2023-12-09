package otus.homework.customview.data.datasources

import otus.homework.customview.data.ExpenseEntity
import java.util.UUID
import kotlin.random.Random

class RandomDataSource : ExpensesDataSource {

    override fun getExpenses(max: Int?): List<ExpenseEntity> {
        val expenses = mutableListOf<ExpenseEntity>()
        repeat(max ?: DEFAULT_MAX) { expenses.add(nextExpense()) }
        return expenses
    }

    private fun nextExpense() = ExpenseEntity(
        id = Random.nextInt(),
        name = UUID.randomUUID().toString().substring(0, 5),
        amount = Random.nextInt(-100, 100),
        category = UUID.randomUUID().toString().substring(0, 10),
        time = Random.nextLong(1613419934, 1623419934)
    )

    private companion object {
        const val DEFAULT_MAX = 10
    }
}