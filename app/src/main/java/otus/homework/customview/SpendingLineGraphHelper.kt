package otus.homework.customview

import otus.homework.customview.utils.addDays
import java.util.concurrent.TimeUnit

object SpendingLineGraphHelper {

    const val SPENDING_INTERVAL = 5_000

    /**
     * подсчитать кол-во меток по оси Y
     */
    fun calculateYMarksCount(spending: List<CategorySpending>): Int {
        val maxSpending = spending.maxOf { it.amount }
        return (maxSpending / SPENDING_INTERVAL).toInt() + 2
    }

    /**
     * подсчитать кол-во меток по оси X
     */
    fun calculateXMarksCount(spending: List<CategorySpending>): Int {
        return getMinAndMaxDatesAndDaysBetween(spending).third
    }

    fun getCategoryColorToSpendingPerDate(spending: List<CategorySpending>): Map<Int, Map<Long, Float>> {
        val allDates = getAllDatesBetweenSpendingInterval(spending)
        val spendingByColor: Map<Int, List<CategorySpending>> = spending.groupBy { it.category.colorRes }
        val categoryColorToSpendingPerDate = mutableMapOf<Int, Map<Long, Float>>()
        spendingByColor.entries.forEach {
            val datesToSpending = mutableMapOf<Long, Float>()
            allDates.forEach { date ->
                datesToSpending[date] =
                    it.value.find { categorySpending -> categorySpending.date == date }?.amount ?: 0f
            }
            categoryColorToSpendingPerDate[it.key] = datesToSpending
        }
        return categoryColorToSpendingPerDate
    }

    fun getAllDatesBetweenSpendingInterval(spending: List<CategorySpending>): Set<Long> {
        val tripleWithDates = getMinAndMaxDatesAndDaysBetween(spending)
        val minDate = tripleWithDates.first
        val daysBetween = tripleWithDates.third
        val setWithDates = mutableSetOf<Long>()
        repeat(daysBetween) {
            setWithDates.add(minDate.addDays(it))
        }
        return setWithDates
    }

    fun getAllAmountsTexts(spending: List<CategorySpending>): List<Int> {
        val allAmounts = mutableListOf<Int>()
        val initialAmount = 0
        repeat(calculateYMarksCount(spending)) {
            allAmounts.add(initialAmount + SPENDING_INTERVAL * it)
        }
        return allAmounts
    }

    /**
     * Получить [Triple] с
     * минимальной датой, максимальной датой
     * и кол-вом дней между этими датами (мин. и макс. даты вкл.)
     */
    private fun getMinAndMaxDatesAndDaysBetween(spending: List<CategorySpending>): Triple<Long, Long, Int> {
        val minDate = spending.minOf { it.date }
        val maxDate = spending.maxOf { it.date }
        val daysBetween = TimeUnit.MILLISECONDS.toDays(maxDate - minDate).toInt() + 1
        return Triple(minDate, maxDate, daysBetween)
    }
}