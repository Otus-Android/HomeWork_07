package otus.homework.customview.presentation.pie.chart

import android.os.Parcelable
import androidx.annotation.ColorInt
import kotlinx.parcelize.Parcelize

/**
 * Данные узла круговой диаграмы
 *
 * @param value значение
 * @param label подпись
 * @param color цвет узла
 */
@Parcelize
data class PieNode(
    val value: Float,
    val label: String? = null,
    @ColorInt val color: Int,
    val payload: Parcelable?
) : Parcelable {

}
