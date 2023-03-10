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
      val points = items.map {
        DynamicChartPoint(
          it.amount,
          it.date / (endDate - startDate).toFloat()
        )
      }
      DynamicChartLine(cat, points)
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
  val amount: Int,
  val datePosition: Float
)
