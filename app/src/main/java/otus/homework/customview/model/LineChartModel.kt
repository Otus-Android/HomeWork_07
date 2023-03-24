package otus.homework.customview.model

import android.os.Parcelable
import androidx.annotation.ColorInt
import kotlinx.parcelize.Parcelize

@Parcelize
data class LineChartModel(
    val category: String,
    val entities: List<Pair<Long, Int>>,
    @ColorInt val color: Int
) : Parcelable
