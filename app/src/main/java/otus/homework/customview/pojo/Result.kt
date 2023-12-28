package otus.homework.customview.pojo

sealed class Result {
    class Error(val throwable: Throwable) : Result()
    class Expenses(val allExpenses: List<Expense>): Result()
    //class ExpensesCategory(val expensesByCategory: Map<String, Int>): Result()
}
