package otus.homework.customview.pieChart

import android.graphics.Canvas
import android.os.Parcelable
import android.view.MotionEvent
import android.widget.Toast
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
class ChartState(
    // сектор, который будет уменьшаться
    var unSelectedChartPart: @RawValue ChartPart? = null,
    // сектор, который будет увеличиваться
    var selectedChartPart: @RawValue ChartPart? = null,
    // центральная часть круга
    var chartCenter: @RawValue ChartCenter? = null,
    val chartParts: @RawValue MutableList<ChartPart> = mutableListOf()
) : Parcelable {

    fun drawCenterCircle(canvas: Canvas, viewInfo: ViewInfo) {
        chartCenter?.draw(canvas, viewInfo)
    }

    fun drawSectors(canvas: Canvas, viewInfo: ViewInfo) {
        var startAngle = 0f
        chartParts.forEach {
            it.startAngle = startAngle
            startAngle += it.sweepAngle

            // рисуем все части кроме кликнутой
            if (selectedChartPart?.name != it.name &&
                unSelectedChartPart?.name != it.name
            ) {
                it.draw(canvas, viewInfo)
            }
        }

        // сначала рисуем часть которая скрывается
        unSelectedChartPart?.draw(canvas, viewInfo)
        // рисуем выбранную часть
        selectedChartPart?.draw(canvas, viewInfo)
    }

    fun handleMotionEvent(motionEvent: MotionEvent?, callback: (chartPart: ChartPart) -> Unit): Boolean {
        // получаем часть по которой кликнули
        return chartParts.firstOrNull { it.chartTap(motionEvent) }?.let { clickedPart ->

            // установить часть которую будем уменьшаться
            unSelectedChartPart = selectedChartPart

            selectedChartPart =
                if (clickedPart.name == unSelectedChartPart?.name) {
                    null
                } else {
                    clickedPart
                }

            chartCenter?.selectedAmount = null

            selectedChartPart?.let {
                chartCenter?.selectedAmount = it.amount
                callback.invoke(it)
            }

            true
        } ?: false
    }
}