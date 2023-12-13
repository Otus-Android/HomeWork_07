package otus.homework.customview.presentation.pie.chart

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Данные кругового графика
 *
 * @param nodes узлы кругового графика
 */
@Parcelize
data class PieData(
    val nodes: List<PieNode> = emptyList()
) : Parcelable
