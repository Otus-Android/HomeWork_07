package otus.homework.customview.pojo

sealed class Mode {
    class ExpensesCategory(val expensesByCategory: Map<String,Int>): Mode()
    class SwitchCategory(val category: String): Mode()
    class DetailsCategory(val detailsData: GraphsBuildDetailsData): Mode()
}
