package otus.homework.customview.presentation.line.chart

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Данные узла линейного диаграмы
 *
 * @param value значение
 * @param time время (unix метка, ms)
 * @param label подпись
 */
@Parcelize
data class LineNode(
    val value: Float,
    val time: Long,
    val label: String?
) : Parcelable
