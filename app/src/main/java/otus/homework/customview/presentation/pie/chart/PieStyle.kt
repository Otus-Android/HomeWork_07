package otus.homework.customview.presentation.pie.chart

/**
 * Стиль отображения кругового графика
 *
 * @param isFilled признак заполненности графика
 */
enum class PieStyle(internal val isFilled: Boolean) {

    /** Стандартный стиль кругового графика */
    PIE(true),

    /** Cтиль графика в виде "бублика" */
    DONUT(false)
}