package otus.homework.customview.presentation.line.chart

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Данные линейного графика
 *
 * @param name наименование графика
 * @param nodes узлы линейного графика
 */
@Parcelize
data class LineData(
    val name: String? = null,
    val nodes: List<LineNode> = emptyList()
) : Parcelable
