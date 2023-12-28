package otus.homework.customview.sealed

import otus.homework.customview.pojo.Expense

sealed class Result {
    class Error(val throwable: Throwable) : Result()
    class Expenses(val allExpenses: List<Expense>): Result()
}
