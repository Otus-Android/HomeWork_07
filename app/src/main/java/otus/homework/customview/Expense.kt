package otus.homework.customview

import java.io.Serializable

data class Expense(
    val id: Int,
    val name: String,
    val amount: Int,
    val category: String,
    val time: Long
): Serializable

class Item(
    val name: String,
    val amount: Int
)

class ItemList(
    val pieces: List<Item>,
    val total: Int,
){
    val onePercent: Float
        get() = total.toFloat()/100
}


