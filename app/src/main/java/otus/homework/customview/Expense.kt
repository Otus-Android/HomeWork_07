package otus.homework.customview

import java.io.Serializable

class Expense(
    val id: Int,
    val name: String,
    val amount: Int,
    val category: String,
    val time: Long
): Serializable

class ListOfExpenses(
    val expenses: ArrayList<Expense>
): Serializable