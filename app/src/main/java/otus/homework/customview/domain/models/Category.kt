package otus.homework.customview.domain.models

/**
 * Категория расходов
 *
 * @param name наименование категории
 * @param amount кол-во расходов
 * @param expenses расходы, относящиеся к категории
 */
data class Category(
    val name: String,
    val amount: Long,
    val expenses: List<Expense>
)
