package otus.homework.customview.pieChart

import android.graphics.Canvas
import android.os.Parcelable
import android.view.MotionEvent
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import otus.homework.customview.ViewInfo

@Parcelize
class PieChartState(
    // сектор, который будет уменьшаться
    var unSelectedPieChartSector: @RawValue PieChartSector? = null,
    // сектор, который будет увеличиваться
    var selectedPieChartSector: @RawValue PieChartSector? = null,
    // центральная часть круга
    var pieChartCenter: @RawValue PieChartCenter? = null,
    val pieChartSectors: @RawValue MutableList<PieChartSector> = mutableListOf()
) : Parcelable {

    fun drawCenterCircle(canvas: Canvas, viewInfo: ViewInfo) {
        pieChartCenter?.draw(canvas, viewInfo)
    }

    fun drawSectors(canvas: Canvas, viewInfo: ViewInfo) {
        var startAngle = 0f
        pieChartSectors.forEach {
            it.startAngle = startAngle
            startAngle += it.sweepAngle

            // рисуем все части кроме кликнутой
            if (selectedPieChartSector?.name != it.name &&
                unSelectedPieChartSector?.name != it.name
            ) {
                it.draw(canvas, viewInfo)
            }
        }

        // сначала рисуем часть которая скрывается
        unSelectedPieChartSector?.draw(canvas, viewInfo)
        // рисуем выбранную часть
        selectedPieChartSector?.draw(canvas, viewInfo)
    }

    fun handleMotionEvent(motionEvent: MotionEvent?, callback: (pieChartSector: PieChartSector) -> Unit): Boolean {
        // получаем часть по которой кликнули
        return pieChartSectors.firstOrNull { it.chartTap(motionEvent) }?.let { clickedPart ->

            // установить часть которую будем уменьшаться
            unSelectedPieChartSector = selectedPieChartSector

            selectedPieChartSector =
                if (clickedPart.name == unSelectedPieChartSector?.name) {
                    null
                } else {
                    clickedPart
                }

            pieChartCenter?.selectedAmount = null

            selectedPieChartSector?.let {
                pieChartCenter?.selectedAmount = it.amount
                callback.invoke(it)
            }

            true
        } ?: false
    }
}