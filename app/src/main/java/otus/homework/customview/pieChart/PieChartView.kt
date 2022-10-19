package otus.homework.customview.pieChart

import android.content.Context
import android.graphics.Canvas
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import otus.homework.customview.BaseChartView
import otus.homework.customview.ViewInfo
import kotlin.math.min

class PieChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseChartView(context, attrs) {

    private var chartState = PieChartState()

    // событие касания
    private var motionEvent: MotionEvent? = null

    private val chartAnimator = ChartAnimator() { animationResult ->
        chartState.selectedPieChartSector?.animate(
            animAngle = animationResult[ChartAnimator.angleKeyInc] ?: 0f,
            animStroke = animationResult[ChartAnimator.strokeKeyInc] ?: 0f
        )
        chartState.unSelectedPieChartSector?.animate(
            animAngle = animationResult[ChartAnimator.angleKeyDec] ?: 0f,
            animStroke = animationResult[ChartAnimator.strokeKeyDec] ?: 0f
        )

        invalidate()
    }

    /** Метод установки значений из json*/
    fun drawChartParts(data: List<PieChartSector>) {

        data.firstOrNull()?.let {
            chartState.pieChartCenter = PieChartCenter(it.totalAmount)
        }

        chartState.pieChartSectors.clear()
        chartState.pieChartSectors.addAll(data)
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (MotionEvent.ACTION_DOWN == event.action) {
            motionEvent = event
            performClick()
            true
        } else {
            false
        }
    }

    override fun performClick(): Boolean {
        super.performClick()

        val callback = { pieChartSector: PieChartSector ->
            Toast.makeText(context, pieChartSector.name, Toast.LENGTH_SHORT).show()
        }
        if (chartState.handleMotionEvent(motionEvent, callback)) {
            chartAnimator.startAnimation()
        }

        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        chartState.drawCenterCircle(canvas, viewInfo)
        chartState.drawSectors(canvas, viewInfo)
    }

    override fun onSaveInstanceState(): Parcelable {
        super.onSaveInstanceState()
        return BaseSavedState(chartState)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        chartState = (state as BaseSavedState).superState as PieChartState
        super.onRestoreInstanceState(state)
    }

}