package otus.homework.customview

interface SpendingRepository {
    fun getCategoriesOverallSpending(): List<CategoryOverallSpending>
    fun getCategoriesSpendingPerDate(): List<CategorySpending>
}