package otus.homework.customview.ui

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class LinearChartModel(
  internal val pairs: List<Pair<String, List<LinearChartItem>>>
) : Parcelable {

  @IgnoredOnParcel
  internal val maxAmount = pairs.map { it.second }.maxOfOrNull { items -> items.sumBy { it.amount } } ?: 0

  @IgnoredOnParcel
  internal val startDate = pairs.map { it.second }.minOfOrNull { items -> items.minOf { it.date } } ?: 0

  @IgnoredOnParcel
  internal val endDate = pairs.map { it.second }.maxOfOrNull { items -> items.maxOf { it.date } } ?: 0

  @IgnoredOnParcel
  internal val lines = mutableListOf<LinearChartLine>()

  init {
    pairs.forEach { (cat, items) ->
      var increasedAmount = 0
      val points = items
        .sortedBy { it.date }
        .map {
          increasedAmount += it.amount
          LinearChartPoint(
            increasedAmount / maxAmount.toFloat(),
            (it.date - startDate) / (endDate - startDate).toFloat()
          )
        }
      lines.add(LinearChartLine(cat, points))
    }
  }
}

@Parcelize
data class LinearChartItem(
  val amount: Int,
  val date: Long
) : Parcelable

internal class LinearChartLine(
  val name: String,
  val points: List<LinearChartPoint>
)

internal data class LinearChartPoint(
  val amountPosition: Float,
  val datePosition: Float
)
