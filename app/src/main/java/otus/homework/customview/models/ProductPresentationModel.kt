package otus.homework.customview.models

import android.content.Context
import android.graphics.Paint
import otus.homework.customview.Event
import otus.homework.customview.presentation.customview.CustomViewUtils.dpToPx

/**
 * Модель для хранения информации о рисуемом продукте на круговой диаграмме.
 * В круговой диаграмме 360 градусов
 *
 * @param percentRatio процентное соотношение продукта из всех купленных продуктов
 * @param previousPercentRation процентное соотношение предыдущего продукта из всех купленных продуктов
 * (для поиска точки старта на круговой диаграмме)
 * @param event событие клика на выбранный продукт
 * @param lineColor цвет линии продукта
 * @param stroke ширина линии продукта
 *
 * @author Евтушенко Максим 26.11.2023
 */
data class ProductPresentationModel(
    private val percentRatio: Float,
    private val previousPercentRation: Float,
    private var lineColor: Int,
    private var stroke: Int = 0,
    val event: Event
) {

    /**
     * Процент занимаемой части круговой диаграммы
     */
    val percentOfCircle: Float
        get() {
            // Расчет переданного значения на круговой диаграмме.
            return 360f * percentRatio / 100f
        }

    /**
     * Процент с которого начинается [percentOfCircle]
     */
    val percentOfStartPosition: Float
        get() {
            // Расчет переданного значения на круговой диаграмме.
            return (360f * previousPercentRation / 100f)
        }

    /**
     * Создание стиля для отрисовки продукта на канвасе
     */
    fun createPaint(context: Context) = Paint().apply {
        color = lineColor
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = context.dpToPx(stroke)
    }
}