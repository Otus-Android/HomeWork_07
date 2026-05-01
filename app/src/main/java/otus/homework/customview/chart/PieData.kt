package otus.homework.customview.chart

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Внутренняя модель данных для одного сектора диаграммы.
 *
 * @property category Название категории трат.
 * @property amount Сумма трат в данной категории.
 * @property startAngle Начальный угол сектора в градусах.
 * @property sweepAngle Угловой размер сектора в градусах.
 * @property color Цвет для отрисовки сектора.
 */
@Parcelize
internal data class PieData(
    val category: String,
    val amount: Int,
    val startAngle: Float,
    val sweepAngle: Float,
    val color: Int
) : Parcelable
