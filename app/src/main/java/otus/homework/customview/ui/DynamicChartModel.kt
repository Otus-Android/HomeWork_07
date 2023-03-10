package otus.homework.customview.ui

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class DynamicChartModel(
  internal val pairs: List<Pair<String, List<DynamicChartItem>>>
) : Parcelable {

  @IgnoredOnParcel
  internal val maxAmount = pairs.map { it.second }.maxOfOrNull { items -> items.sumBy { it.amount } } ?: 0

  @IgnoredOnParcel
  internal val startDate = pairs.map { it.second }.minOfOrNull { items -> items.minOf { it.date } } ?: 0

  @IgnoredOnParcel
  internal val endDate = pairs.map { it.second }.maxOfOrNull { items -> items.maxOf { it.date } } ?: 0

  @IgnoredOnParcel
  internal val lines = mutableListOf<DynamicChartLine>()

  init {
    pairs.forEach { (cat, items) ->
      var increasedAmount = 0
      val points = items
        .sortedBy { it.date }
        .map {
          increasedAmount += it.amount
          DynamicChartPoint(
            increasedAmount / maxAmount.toFloat(),
            (it.date - startDate) / (endDate - startDate).toFloat()
          )
        }
      lines.add(DynamicChartLine(cat, points))
    }
  }
}

@Parcelize
data class DynamicChartItem(
  val amount: Int,
  val date: Long
) : Parcelable

internal class DynamicChartLine(
  val name: String,
  val points: List<DynamicChartPoint>
)

internal data class DynamicChartPoint(
  val amountPosition: Float,
  val datePosition: Float
)
