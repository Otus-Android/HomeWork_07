package otus.homework.customview.domain.models

data class Category(
    val name: String,
    val amount: Long,
    val expenses: List<Expense>
)
