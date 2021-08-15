package otus.homework.customview.presenter

import java.time.LocalDate

data class ExpenseByDay(
    val amount:Int,
    val date: LocalDate,
    val category:String
)