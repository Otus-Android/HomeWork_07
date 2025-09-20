package otus.homework.customview.data

import android.graphics.Color
import otus.homework.customview.models.PieChartData
import otus.homework.customview.presentation.PieChartSpendingItem
import otus.homework.customview.presentation.PieChartUiInfo
import javax.inject.Inject

class UiDataMapper @Inject constructor() {
    val colorsMap = mutableMapOf(
        "Алкоголь" to Color.BLACK,
        "Продукты" to Color.RED,
        "Здоровье" to Color.BLUE,
        "Кафе и рестораны" to Color.YELLOW,
        "Доставка еды" to Color.GREEN,
        "Транспорт" to Color.CYAN,
        "Спорт" to Color.MAGENTA
    )

    fun map(data: PieChartData): PieChartUiInfo {
        var allSum = 0
        val generalCategorySumMap = mutableMapOf<String, Int>()

        data.items.forEach { item ->
            allSum += item.amount
            var currentSum = generalCategorySumMap.getOrDefault(item.category, 0)
            currentSum += item.amount
            generalCategorySumMap.put(item.category, currentSum)
        }

        val uiItems = generalCategorySumMap.map { entry ->
            PieChartSpendingItem(
                color = colorsMap.getOrDefault(entry.key, Color.TRANSPARENT),
                amount = generalCategorySumMap.getOrDefault(entry.key, 0),
                total = allSum,
                category = entry.key
            )
        }
        return PieChartUiInfo(
            spendingInfo = uiItems
        )
    }
}
