package otus.homework.customview.data.datasources

import android.content.res.Resources
import otus.homework.customview.R
import otus.homework.customview.data.models.ExpenseEntity
import java.util.UUID
import kotlin.random.Random

/**
 * Источник данных случайных записей по расходам
 *
 * @param resources менеджер ресурсов
 */
class RandomDataSource(
    private val resources: Resources,
) : ExpensesDataSource {

    override fun getExpenses(max: Int?): List<ExpenseEntity> {
        val categories = resources.getStringArray(R.array.stub_categories)
        val prefix = resources.getString(R.string.name_prefix)
        val size = max ?: DEFAULT_MAX
        val expenses = mutableListOf<ExpenseEntity>()
        for (i in 0 until size) {
            val expense = nextExpense(i, prefix, categories)
            expenses.add(expense)
        }
        return expenses
    }

    private fun nextExpense(id: Int, prefix: String, categories: Array<String>) = ExpenseEntity(
        id = id,
        name = "$prefix-${UUID.randomUUID().toString().substring(0, 5)}",
        amount = Random.nextInt(0, 100),
        category = categories.random(),
        time = Random.nextLong(1613419934, 1623419934)
    )

    private companion object {

        /** Стандартное значение максимально возможного кол-ва записей */
        const val DEFAULT_MAX = 10
    }
}