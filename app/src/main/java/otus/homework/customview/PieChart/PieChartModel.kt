package otus.homework.customview.PieChart

import android.graphics.Color
import android.graphics.CornerPathEffect
import android.graphics.Paint
import android.util.Log

data class PieChartModel(
    var percentOfCircle: Float = 0F,
    var percentToStartAt: Float = 0F,
    var colorOfLine: Int = 0,
    var stroke: Float = 0F,
    var paint: Paint = Paint(),
    var paintRound: Boolean = false
){
    /**
     * Блок, в котором значения преобразуются к приближенным значениям круговой диаграммы.
     * То есть в модель передается процент (от 0 до 100).
     */
    init {
        // Проверка на корректность переданного процента.
        if (percentOfCircle < 0 || percentOfCircle > 100) {
            percentOfCircle = 100F
        }

        // Расчет переданного значения на круговой диаграмме.
        percentOfCircle = 360 * percentOfCircle / 100

        // Проверка на корректность переданного процента.
        if (percentToStartAt < 0 || percentToStartAt > 100) {
            percentToStartAt = 0F
        }

        // Расчет переданного значения на круговой диаграмме.
        percentToStartAt = 360 * percentToStartAt / 100

        /** Установка своего цвета в случаи пропуска [colorOfLine]  */
        if (colorOfLine == 0) {
            colorOfLine = Color.parseColor("#000000")
        }

        // Инициализация кисти для отрисовки
        paint = Paint()
        paint.color = colorOfLine
        paint.isAntiAlias = true
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = stroke
        paint.isDither = true;

        // Проверка необходимости закругления концов линии объекта.
        if (paintRound){
            paint.strokeJoin = Paint.Join.ROUND;
            paint.strokeCap = Paint.Cap.ROUND;
            paint.pathEffect = CornerPathEffect(8F);
        }

        Log.d("Angle", "percentToStartAt = $percentToStartAt percentOfCircle = $percentOfCircle ${colorOfLine.toString()}")
    }
}