package otus.homework.customview

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import otus.homework.customview.pojo.*
import otus.homework.customview.util.Converter
import otus.homework.customview.util.Serializer


class ChartViewModel : ViewModel() {

    private lateinit var _allExpenses: List<Expense>

    private val expensesByCategory = mutableMapOf<String, Int>()

    private val _parseExpensesResult = MutableLiveData<Result>()
    val parseExpensesResult: LiveData<Result> get() = _parseExpensesResult

    private val _mode = MutableLiveData<Mode>()
    val mode: LiveData<Mode> get() = _mode

    fun getExpenseData(context: Context) {
        _parseExpensesResult.value = Serializer.deserialize(context)
    }


    //Переделать чтобы сразу с Sector было
    fun showExpensesByCategory(allExpenses: List<Expense>) {
        _allExpenses = allExpenses
        for (i in allExpenses.indices) {
            val category = allExpenses[i].category
            if (
                expensesByCategory.computeIfPresent(category) { _, amount ->
                    amount + allExpenses[i].amount
                } == null
            ) expensesByCategory[category] = allExpenses[i].amount
        }
        _mode.value = Mode.ExpensesCategory(expensesByCategory)
    }

    fun showDetailsCategory(category: String, color: Int) {
        val detailsCategory = _allExpenses.filter { it.category == category }
            .map { it.copy(time = Converter.timestampToDays(it.time)) }
            .groupBy { it.time }
            .mapValues { entry ->
                with(entry.value) {
                    Details(
                        first().name,
                        sumOf { it.amount },
                        first().time
                    )
                }
            }
            .values
            .toList()

        val rangeDate = Pair(_allExpenses.minOf { it.time }, _allExpenses.maxOf { it.time })
        val rangeAmount = Pair(_allExpenses.minOf { it.amount }, _allExpenses.maxOf { it.amount })

        _mode.value = Mode.DetailsCategory(
            GraphsBuildDetailsData(category, color, rangeDate, rangeAmount, detailsCategory)
        )
    }
}