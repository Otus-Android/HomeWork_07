package otus.homework.customview.piechart

import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.lifecycle.MutableLiveData
import otus.homework.customview.piechart.ChartModel
import otus.homework.customview.piechart.TouchView
import kotlin.math.pow

class PieChartView : View {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attributeSet: AttributeSet?) : super(context, attributeSet)

    var chartModel = ChartModel()

    lateinit var cycleCenter: PointF
    var _clickSector = MutableLiveData<Int>(-1)

    private val gestureDetector = GestureDetector(context, TouchView())
    private val pChart = Paint()
    private val pText = Paint()
    private val rec = RectF()
    private val color = arrayOf(
        Color.RED,
        Color.BLUE,
        -0x886699,
        -0x11BB88,
        Color.CYAN,
        Color.GRAY,
        Color.YELLOW,
        Color.MAGENTA,
        Color.GREEN
    )
    private var startLeft = 0f
    private var startTop = 0f
    private var midHeight = 0f
    private var midWidth = 0f

    init {
        rec.left = 0f
        rec.top = 0f
        isSaveEnabled = true
        pChart.isAntiAlias = true
        pChart.style = Paint.Style.STROKE
        pChart.strokeWidth = 40f
        pText.textSize = 20f
        pText.style = Paint.Style.FILL_AND_STROKE
        pText.setStrokeWidth(2f)

    }

    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        bundle.putParcelable("superState", super.onSaveInstanceState())
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        var viewState = state
        if (viewState is Bundle) {
            viewState = viewState.getParcelable("superState")
        }
        super.onRestoreInstanceState(viewState)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val size = Math.min(getMeasuredWidth(), getMeasuredHeight())
        val finalWidth = size
        val finalHeight = size
        System.out.println("finalWidth $finalWidth finalHeight $finalHeight")
        setMeasuredDimension(finalWidth, finalHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        midHeight = Math.min(height, width) / 2f
        midWidth = midHeight
        startLeft = width * 0.1f
        startTop = height * 0.1f
        // Шаблон
        canvas.drawARGB(230, 400, 150, 50)
// Сдвиг для правильного отображения в landscape
        canvas.translate(0f, height / 4f)
// Рисование pieChart
        cycleCenter =
            PointF((midWidth + startLeft) / 2, (midHeight + startLeft) / 2 + height / 4f)
        var j = 0

        chartModel.pieData.forEach { s, _ ->
            pChart.color = color[j % color.size]
            canvas.drawArc(
                startLeft - midWidth * (chartModel.scaleArc[j] - 1f),
                startTop - midHeight * (chartModel.scaleArc[j] - 1f),
                midWidth * chartModel.scaleArc[j],
                midHeight * chartModel.scaleArc[j],
                chartModel.beginArc.get(j), chartModel.lengthArc.get(j),
                false,
                pChart
            )
// В таблицу наименований
            canvas.translate(midWidth * 1.2f, j * 50f)
            if (j == chartModel.checkedIndex) {
                textDraw(canvas, pText, pChart, s + " !!")
            } else {
                textDraw(canvas, pText, pChart, s)
            }
            canvas.translate(-midWidth * 1.2f, -j * 50f)
            j++
        }
    }

    private fun textDraw(canvas: Canvas, p: Paint, pBack: Paint, text: String) {
        val rect = Rect(20, 20, 170, 23)
        canvas.drawRect(rect, pBack)
        canvas.drawText(text, 10f, 25f, p)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        val angle: Double
        val lenghtX = event.x - cycleCenter.x
        val lenghtY = event.y - cycleCenter.y
        val touchVector = Math.sqrt(
            (lenghtX.pow(2) + lenghtY.pow(2)).toDouble()
        )
        if (touchVector < midHeight / 2) {
            angle = (Math.atan((lenghtY / lenghtX).toDouble()) / Math.PI * 180).let {
                if (lenghtX < 0f) {
                    it.plus(180.0)
                } else {
                    if (lenghtY < 0f) {
                        it.plus(360.0)
                    } else {
                        it
                    }
                }
            }
            run breaking@{
                chartModel.beginArc.forEachIndexed { index, beginAngle ->
                    if (beginAngle > angle) {
                        _clickSector.value = index - 1
                        return@breaking
                    }
                }
                _clickSector.value = chartModel.beginArc.size - 1
            }
        }
        return super.onTouchEvent(event)
    }
}