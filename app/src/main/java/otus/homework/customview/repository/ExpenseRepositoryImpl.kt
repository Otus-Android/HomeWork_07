package otus.homework.customview.repository

import android.content.Context
import com.google.gson.Gson
import io.reactivex.Single
import otus.homework.customview.R
import otus.homework.customview.entity.Expense

class ExpenseRepositoryImpl(private val context: Context) : ExpenseRepository {


    override fun getAllExpenses(): Single<Expense> {
        val result: String = context.resources.openRawResource(R.raw.payload).bufferedReader().use {
            it.readText()
        }
        val gson = Gson()
        val expense = gson.fromJson(result, Expense::class.java)
        return Single.just(expense)
    }
}