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
        val minDate: Calendar? = items.minOfOrNull { it.x }

        fun getDatesByMonth(month: Int): List<Pair<Int, Int>> {
            return sortedItems[month]?.map { Pair(it.x.get(Calendar.DAY_OF_MONTH), it.y) }
                ?: emptyList()
        }

        fun getMaxDateByMonth(month: Int): Calendar? {
            return sortedItems[month]?.maxOfOrNull { it.x }
        }

        fun getMinDateByMonth(month: Int): Calendar? {
            return sortedItems[month]?.minOfOrNull { it.x }
        }

        fun getMaxValueByMonth(month: Int): Int {
            return sortedItems[month]?.maxOfOrNull { it.y } ?: 0
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