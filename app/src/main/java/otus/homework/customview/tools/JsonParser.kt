package otus.homework.customview.tools

import android.content.Context
import com.google.gson.Gson
import otus.homework.customview.*
import otus.homework.customview.entities.Category
import otus.homework.customview.entities.Spending

class JsonParser(private val context: Context) {

    private fun spendingListFromJson(): Either<String, Array<Spending>> = try {
        val payload =
            context.resources.openRawResource(R.raw.payload).bufferedReader().use { it.readText() }
        val spendingList =
            Gson().fromJson(payload.trimIndent(), Array<Spending>::class.java)
        spendingList.success()
    } catch (e: Exception) {
        (e.message ?: "Unknown exception").failure()
    }

    fun sortedSpendingListByTime(name: String): Either<String, List<Spending>> =
        when (val spendingList = spendingListFromJson()) {
            is Either.Success -> {
                spendingList.result
                    .sortedBy { it.time }
                    .filter { it.category == name }
                    .success()
            }
            is Either.Failure -> spendingList.error.failure()
        }

    fun sortedCategoriesByTotal(): Either<String, List<Category>> =
        when (val spendingList = spendingListFromJson()) {
            is Either.Success -> {
                val set: Set<Category> =
                    spendingList.result.map { Category(name = it.category) }.toSet()
                set.forEach { setItem ->
                    setItem.total = spendingList.result
                        .filter { it.category == setItem.name }
                        .map { it.amount }
                        .fold(0) { total, item -> total + item }
                }
                val sortedList = set.sortedByDescending { it.total }
                val pieColors = PieColors(set.size).colors()
                fillColorInCategories(sortedList, pieColors)
                sortedList.success()
            }
            is Either.Failure -> spendingList.error.failure()
        }

    private fun fillColorInCategories(list: List<Category>, pieColors: IntArray) {
        var id = 0
        list.forEachIndexed { index, category ->
            category.color = pieColors[index]
            category.id = id++
        }
    }

}