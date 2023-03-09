package otus.homework.customview.ui

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DynamicChartModel(
  internal val lines: List<DynamicChartLine>
) : Parcelable

@Parcelize
data class DynamicChartLine(
  val name: String,
  val items: List<DynamicChartItem>
) : Parcelable

@Parcelize
data class DynamicChartItem(
  val amount: Int,
  val date: Long
) : Parcelable
