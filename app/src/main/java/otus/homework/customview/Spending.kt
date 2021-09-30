package otus.homework.customview

import java.util.Calendar

/**
 *
 *
 * @author Юрий Польщиков on 27.09.2021
 */
data class Spending(
    val amount: Double,
    val category: Category,
    val name: String = "",
    val percent: Double = 0.0,
    val time: Calendar? = null
)
