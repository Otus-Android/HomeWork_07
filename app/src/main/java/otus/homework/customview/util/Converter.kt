package otus.homework.customview.util

import otus.homework.customview.extensions.format
import java.util.*

object Converter {
    private const val DAY_SECONDS = 86400L

    fun rangeTimestampToDaysList(rangeTimestamp: Pair<Long, Long>): List<Long> {
        val startDay = rangeTimestamp.first / DAY_SECONDS
        val endDay = rangeTimestamp.second / DAY_SECONDS
        val dayQuantity = (endDay - startDay + 1).toInt()

        val dayList = mutableListOf<Long>()

        for (i in 0 until  dayQuantity) {
            val day = when (i)  {
                0 -> startDay
                dayQuantity - 1 -> endDay
                else -> startDay + i
            }
            dayList.add(day)
        }
        return dayList
    }

    fun rangeIntToIntListAccordingDivision(maxAmount: Int, divisionsQuantity: Int): List<Int> {
        val division = maxAmount / divisionsQuantity
        val divisionData = mutableListOf<Int>()

        for (i in 0..divisionsQuantity) {
            divisionData.add(i * division)
        }
        return divisionData
    }

    fun timestampToDateString(timestamp: Long): String {
        val date = Date(timestamp * 1000)
        return date.format()
    }

    fun timestampToDays(timestamp: Long): Long {
        return timestamp / (3600 * 24)
    }
}