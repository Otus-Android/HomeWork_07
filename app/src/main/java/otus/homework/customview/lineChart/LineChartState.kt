package otus.homework.customview.lineChart

import androidx.annotation.ColorInt
import java.util.*

sealed interface LineChartState {

    data class Dates(
        private val items: List<LineChartItem<Calendar>>,
        @ColorInt val color: Int
    ) : LineChartState {

        val sortedItems = items.sortedBy { it.x }
            .groupBy { it.x.get(Calendar.MONTH) }
        val maxDate: Calendar? = items.minOfOrNull { it.x }
        val minDate: Calendar? = items.maxOfOrNull { it.x }
        val minValue: Int = items.minOfOrNull { it.y } ?: 0
        val maxValue: Int = items.maxOfOrNull { it.y } ?: 0

        fun getDatesByMonth(month: Int): List<Pair<Int, Int>> {
            return sortedItems[month]?.map { Pair(it.x.get(Calendar.DAY_OF_MONTH), it.y) }
                ?: emptyList()
        }

    }

    data class LineChartItem<T>(
        val x: T,
        val y: Int
    )

    companion object {
        fun LineChartState.requireDates() = this as Dates
        fun default() = LineChartState.Dates(emptyList(), 0x00000000)
    }

}