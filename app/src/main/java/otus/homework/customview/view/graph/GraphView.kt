package otus.homework.customview.view.graph

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.annotation.RequiresApi
import otus.homework.customview.presenter.ExpenseByDay
import otus.homework.customview.view.CategoryToPaint
import java.time.LocalDate

interface GraphView {

    fun showExpense(expenseByDay: Map<LocalDate, ExpenseByDay>, maxAmount: Int)
    fun showView()
    fun goneView()
}

const val COUNT_OF_VALUE_X_AXIS = 7
const val COUNT_OF_VALUE_Y_AXIS = 5
const val SEGMENT_LENGTH = 5f

class GraphViewImpl @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), GraphView {

    private var _coordZeroX = 25f// начало оси X
    private var _coordZeroY = 1f// начало оси Y
    private var _coordEndXAxis = 0f//Конец оси Х
    private var _coordEndYAxis = 0f//Конец оси Y
    private var _stepXAxisValue = 0f//Расстояние между подписями оси X
    private var _stepYAxisValue = 0f//Расстояние между подписями оси Y
    private var _maxValueYAxis = 25000//максимально значение по оси Y
    private var _stepValueYAxis = _maxValueYAxis / COUNT_OF_VALUE_Y_AXIS//шаг значение по оси Y
    private var _valueAxisY: Float = 0f// масштаб оси Y

    private val _listExpenseByDay = mutableMapOf<LocalDate, ExpenseByDay>()


    private val paintAxis = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL
        strokeWidth = 2f
    }

    private val paintAxisDottedLine = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL
        strokeWidth = 1f
        pathEffect = DashPathEffect(floatArrayOf(5f, 15f), 0f)
    }

    private val paintText = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL
        textSize = 22f
    }

    private val paintPoint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val heightView = heightSize / 3

        _coordZeroY = heightView - 25f
        _valueAxisY = _coordZeroY / _maxValueYAxis
        _coordEndXAxis = widthSize.toFloat() - _coordZeroX
        _coordEndYAxis = 0f//Конец оси Y

        _stepXAxisValue = _coordEndXAxis / COUNT_OF_VALUE_X_AXIS.toFloat()
        _stepYAxisValue = _coordZeroY / COUNT_OF_VALUE_Y_AXIS.toFloat()

        setMeasuredDimension(widthSize, heightView)
    }


    override fun showExpense(expenseByDay: Map<LocalDate, ExpenseByDay>, maxAmount: Int) {
        _listExpenseByDay.clear()
        _listExpenseByDay.putAll(expenseByDay)

        val rangeYAxis = 0..100_000 step 5000
        for (value in rangeYAxis) {
            if (maxAmount < value) {
                _maxValueYAxis = value
                _stepValueYAxis = _maxValueYAxis / COUNT_OF_VALUE_Y_AXIS
                _valueAxisY = _coordZeroY / _maxValueYAxis
                break
            }
        }

        invalidate()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawCoordinateGrid(canvas)
        drawGraph(canvas)
    }

    private fun drawGraph(canvas: Canvas) {
        var startLineX: Float
        var startLineY: Float
        var endLineX = 0f
        var endLineY = 0f

        for ((index, date) in _listExpenseByDay.keys.withIndex()) {

            _listExpenseByDay[date]?.let {
                CategoryToPaint.getPaint(it.category)?.let {
                    paintPoint.apply {
                        color = it.color
                    }
                }

                canvas.drawCircle(
                    (index + 1) * _stepXAxisValue,
                    _coordZeroY - (_valueAxisY * it.amount),
                    7f,
                    paintPoint
                )

                startLineX = endLineX
                startLineY = endLineY
                endLineX = (index + 1) * _stepXAxisValue
                endLineY = _coordZeroY - (_valueAxisY * it.amount)
                if (index > 0) {
                    canvas.drawLine(startLineX, startLineY, endLineX, endLineY, paintPoint)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun drawCoordinateGrid(canvas: Canvas) {
        canvas.drawLine(_coordZeroX, _coordZeroY, _coordZeroX, _coordEndYAxis, paintAxis)
        canvas.drawLine(_coordZeroX, _coordZeroY, _coordEndXAxis, _coordZeroY, paintAxis)
        for (item in 1..COUNT_OF_VALUE_X_AXIS) {
            val startX = item * _stepXAxisValue
            val startY = _coordZeroY - SEGMENT_LENGTH
            val stopY = _coordZeroY + SEGMENT_LENGTH
            canvas.drawLine(startX, startY, startX, stopY, paintAxis)
            canvas.drawLine(startX, stopY, startX, _coordEndYAxis, paintAxisDottedLine)
            _listExpenseByDay.keys.toList().getOrNull(item - 1)?.let {
                canvas.drawText("$it", startX - 20f, stopY + 15f, paintText)
            }
        }

        for (item in 1..COUNT_OF_VALUE_Y_AXIS) {
            val startY = item * _stepYAxisValue
            val startX = _coordZeroX - SEGMENT_LENGTH
            val stopX = _coordZeroX + SEGMENT_LENGTH
            canvas.drawLine(startX, startY, stopX, startY, paintAxis)
            canvas.drawLine(stopX, startY, _coordEndXAxis, startY, paintAxisDottedLine)
            canvas.drawText("${_maxValueYAxis - (_stepValueYAxis * item)}", stopX, startY, paintText)
        }
    }

    override fun showView() {
        visibility = VISIBLE
    }

    override fun goneView() {
        visibility = GONE
    }

}