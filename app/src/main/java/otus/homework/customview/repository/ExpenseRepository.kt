package otus.homework.customview.repository

import io.reactivex.Single
import otus.homework.customview.entity.Expense

interface ExpenseRepository {

    fun getAllExpenses(): Single<Expense>
}