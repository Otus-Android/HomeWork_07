package otus.homework.customview.sealed

import otus.homework.customview.pojo.GraphsBuildDetailsData
import otus.homework.customview.pojo.Sector

sealed class Mode {
    class ExpensesCategory(val sectorsByCategory: Map<String, Sector>): Mode()
    class DetailsCategory(val detailsData: GraphsBuildDetailsData): Mode()
}
