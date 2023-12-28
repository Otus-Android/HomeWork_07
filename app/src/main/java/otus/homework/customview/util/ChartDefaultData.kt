package otus.homework.customview.util

import android.graphics.Color
import otus.homework.customview.pojo.Sector

object ChartDefaultData {

    fun getChartColors(): Array<Int> {
        return arrayOf(
            Color.BLUE,
            Color.CYAN,
            Color.GRAY,
            Color.GREEN,
            Color.RED,
            Color.MAGENTA,
            Color.YELLOW,
            Color.DKGRAY,
            Color.BLACK,
            Color.rgb(100,100,15)
        )
    }

    fun createSectorsByCategory(expensesByCategory: Map<String, Int>): Map<String, Sector> {
        val anglesByCategory = mutableMapOf<String, Sector>()

        val totalAmount = expensesByCategory.values.sum()
        val partPieDegreeKoef = 360 / totalAmount.toFloat()

        var startAngle = 0f
        var partPieDegree: Float

        var colorOrderNumber = 0
        for ((k,v) in expensesByCategory) {
            partPieDegree = v * partPieDegreeKoef
            anglesByCategory[k] =
                Sector(
                    startAngle,
                    partPieDegree,
                    getChartColors()[colorOrderNumber % getChartColors().size]
                )

            startAngle += partPieDegree
            colorOrderNumber++
        }
        return anglesByCategory
    }

    fun createCategoryDetailExpensesListByDate() {

    }
}