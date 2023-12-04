package otus.homework.customview

import java.io.Serializable
import java.text.SimpleDateFormat
import kotlin.random.Random

data class Expense(
    val id: Int,
    val name: String,
    val amount: Int,
    val category: String,
    val time: Long
): Serializable

class AllExpenses(private val allExpenses: List<Expense>) {
    val mapByCategory: Map<String, List<Expense>>
    var total: Int = 0

    init {
        mapByCategory = sortByCategory()
    }


    fun sortByCategory(): Map<String, List<Expense>> {
        val mapByCategory = hashMapOf<String, MutableList<Expense>>()
        allExpenses.forEach { expense ->
            total += expense.amount
            if (!mapByCategory.containsKey(expense.category)) {
                mapByCategory[expense.category] = mutableListOf()
                mapByCategory[expense.category]!!.add(expense)
            } else {
                mapByCategory[expense.category]!!.add(expense)
            }
        }
        val dateComparator =
            Comparator<Expense> { o1, o2 -> if (o1.time > o2.time) 1 else if (o2.time > o1.time) -1 else 0 }

        for (category in mapByCategory.keys) {
            mapByCategory[category]!!.sortedWith(dateComparator)
        }
        return mapByCategory
    }

    fun getAllCategoriesExpenses(): List<DayExpense> {
        val dayExpenses = mutableListOf<DayExpense>()
        mapByCategory.forEach { entry ->
            dayExpenses.addAll(mouthsExpensesByCategory(entry.key))
        }
        repeat(30 - dayExpenses.size){
            dayExpenses.add(DayExpense(Random.nextInt(1),"01/12/2023"))
        }
        return dayExpenses
    }

    fun getOneCategoryExpenses(category: String):List<DayExpense>{
        val days = mouthsExpensesByCategory(category) as MutableList
        repeat(30 - days.size){
            days.add(DayExpense(Random.nextInt(1),"01/12/2023"))
        }
        return days
    }


   private fun mouthsExpensesByCategory(category: String):List<DayExpense>{
        val categoryExpenses = mapByCategory[category]?: emptyList()
        val dayExpenses = mutableListOf<DayExpense>()
        var totalByDay = 0
        var prevDay = ""
        categoryExpenses.forEachIndexed { index, expense ->
            val dayExp = expense.mapToDayExpense()
                if (prevDay == "") {
                    prevDay = dayExp.date
                    totalByDay += dayExp.amount
                    if (index == categoryExpenses.lastIndex) {
                        val d = DayExpense(totalByDay, prevDay)
                        dayExpenses.add(d)
                    }
                } else if (dayExp.date == prevDay) {
                    totalByDay += dayExp.amount
                    if (index == categoryExpenses.lastIndex) {
                        val d = DayExpense(totalByDay, prevDay)
                        dayExpenses.add(d)
                    }

                } else {
                    val d = DayExpense(totalByDay, prevDay)
                    dayExpenses.add(d)
                    totalByDay = dayExp.amount
                    prevDay = dayExp.date
                    if (index == categoryExpenses.lastIndex) {
                        val _d = DayExpense(totalByDay, prevDay)
                        dayExpenses.add(_d)
                    }
                }
            }
        return dayExpenses
    }
}


fun Long.toDay(): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy")
    return sdf.format(this)
}
fun Expense.mapToDayExpense(): DayExpense{
    return DayExpense(this.amount,
        this.time.toDay())
    }


class DayExpense(
    val amount: Int,
    val date: String
){

}




data class Item(
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




