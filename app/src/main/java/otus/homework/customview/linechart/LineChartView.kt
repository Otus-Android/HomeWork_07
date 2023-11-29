package otus.homework.customview.linechart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import otus.homework.customview.LineChartModel
import java.text.SimpleDateFormat
import java.util.*

class LineChartView : View {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attributeSet: AttributeSet?) : super(context, attributeSet)

    companion object {
        private const val AXIS_X_DIVIDER = 10
        private const val AXIS_Y_DIVIDER = 10
    }

    var lineChartModel = LineChartModel()

    private val gestureDetector = GestureDetector(context, TouchView())
    private val pChart = Paint()
    private val pPoint = Paint()


    private var startX = 0f
    private var startY = 0f
    private var stopX = 0f
    private var stopY = 0f
    private var scaleTime = 1f
    private var scaleAmount = 1f
    private var timeWidth = 0L
    private var lineDX = 0f
    private var lineDY = 0f
    private var graf: MutableList<PointF> = mutableListOf()
    private var startPoint = PointF(startX, stopY)
    private var currentPoint = PointF(0f, 0f)

    init {
        isSaveEnabled = true
        pChart.isAntiAlias = true
        pChart.style = Paint.Style.FILL
        pChart.setColor(Color.WHITE)

        pPoint.style = Paint.Style.FILL_AND_STROKE
        pPoint.setStrokeWidth(20f)
        pPoint.color = Color.BLUE
        timeWidth = lineChartModel.maxTime - lineChartModel.minTime

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
        val finalWidth = (getMeasuredWidth() * 0.9).toInt()
        val finalHeight = (getMeasuredHeight() * 0.9).toInt()
        System.out.println("finalWidth $finalWidth finalHeight $finalHeight")
        setMeasuredDimension(finalWidth, finalHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        startX = width * 0.15f
        startY = 0f
        stopX = width.toFloat()
        stopY = height * 0.9f
        lineDX = (stopX - startX) / AXIS_X_DIVIDER
        lineDY = (stopY - startY) / AXIS_Y_DIVIDER
        scaleTime = if (timeWidth > 0L)
            (stopX - startX) / timeWidth
        else
            (stopX - startX) / 2.0f

        try {
            scaleAmount = (stopY - startY) / lineChartModel.maxAmount
        } catch (e: NullPointerException) {
            Toast.makeText(context, "Нет данных ", Toast.LENGTH_LONG).show()
            scaleAmount = 0f
        }

        lineChartModel.grafData.forEach { t, u ->
            System.out.println("t ${convertLongToTime(t)} u $u")
            graf.add(PointF((t - lineChartModel.minTime) * scaleTime, u * scaleAmount))
        }

// Шаблон
        canvas.drawARGB(230, 240, 215, 205)
        canvas.drawRect(startX, startY, stopX, stopY, pChart)
//  Область графика
        pChart.style = Paint.Style.STROKE
        pChart.strokeWidth = 5f
        pChart.setColor(Color.BLACK)
        pChart.textSize = 30f

        canvas.drawRect(startX, startY, stopX, stopY, pChart)
//  Сетка осей
        for (i: Int in 1..AXIS_X_DIVIDER) {
            canvas.drawLine((startX + lineDX * i), startY, (startX + lineDX * i), stopY, pChart)
        }
        for (i: Int in 1..AXIS_Y_DIVIDER) {
            canvas.drawLine(startX, (startY + lineDY * i), stopX, (startY + lineDY * i), pChart)
        }
// Пределы значений по Y
        canvas.drawText(
            "0",
            startX - pChart.textSize,
            stopY,
            pChart
        )
        canvas.drawText(
            lineChartModel.maxAmount.toString(),
            startX - (lineChartModel.maxAmount.toString().length - 1) * pChart.textSize,
            startY + 30f, pChart
        )

// Пределы значений по X
        val startTime = convertLongToTime(lineChartModel.minTime)
        val stopTime = convertLongToTime(lineChartModel.maxTime)
        canvas.drawText(startTime,
               startX,
            stopY + pChart.textSize,
               pChart
        )
        canvas.drawText(
            stopTime,
            stopX - (stopTime.length- 7) * pChart.textSize,
            stopY + pChart.textSize,
            pChart
        )
        System.out.println(" textSize ${lineChartModel.maxAmount.toString().length}")
// Построение графика
        pChart.setColor(Color.RED)
        graf.forEachIndexed() { index, it ->
            System.out.println(" $it")
            currentPoint = PointF(startX + it.x, stopY - it.y)
            if (index > 0) {
                canvas.drawLine(
                    startPoint.x, startPoint.y,
                    currentPoint.x, currentPoint.y,
                    pChart
                )
            }
            canvas.drawPoint(currentPoint.x, currentPoint.y, pPoint)
            startPoint = PointF(currentPoint.x, currentPoint.y)
        }


    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)

        return super.onTouchEvent(event)
    }

    fun convertLongToTime(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("MM.dd HH:mm:ss")
        return format.format(date)
    }


}