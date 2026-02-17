package otus.homework.customview

data class PurchaseItem(
    val id: Int?,
    val name: String?,
    val amount: Int,
    val category: CategoryExpense,
    val time: Long?
)