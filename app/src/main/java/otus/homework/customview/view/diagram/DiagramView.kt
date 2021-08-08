package otus.homework.customview.view.diagram

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import otus.homework.customview.presenter.ExpenseByCategory
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import otus.homework.customview.presenter.ExpensePresenter
import kotlin.math.atan2


interface DiagramView {
    fun showExpenseByCategory(expenseByCategory:List<ExpenseByCategory>)
    fun setExpensePresenter(expensePresenter: ExpensePresenter)

}

const val STROKE_WIDTH = 100f

class DiagramViewImpl : DiagramView, View {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private lateinit var _expensePresenter: ExpensePresenter
    override fun setExpensePresenter(expensePresenter: ExpensePresenter) {
        _expensePresenter = expensePresenter
    }

    private val oval = RectF()
    private val paintList = PaintList(context)
    private val listArcParameters = mutableListOf<ParametersDraw>()
    private val listAngle = mutableListOf<Pair<ClosedFloatingPointRange<Float>, String>>()

    private var left: Float = 100f
    private var top: Float = 500f
    private var right: Float = 100f
    private var bottom: Float = 100f
    private var centerX = left + (right - left) / 2
    private var centerY = top + (bottom - top) / 2
    private var radius = centerX - left + 50

    private val textPaint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 1f
        textSize = 32f
        this.style = Paint.Style.FILL
    }

    @SuppressLint("ResourceAsColor")
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        centerX = widthSize / 2f
        centerY = heightSize / 4f
        left = centerX - 250f
        top = centerY - 250f
        right = centerX + 250f
        bottom = centerY + 250f
        radius = centerX - left + 130

        setMeasuredDimension(widthSize, heightSize / 2)
    }

    @SuppressLint("ResourceAsColor", "DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        oval.set(left, top, right, bottom)
        listArcParameters.forEach {

            canvas.drawArc(oval, it.startAngle, it.sweepAngle, it.useCenter, it.paint)
            val pairStart = showLegend(it)

            canvas.drawText("${it.percent.toInt()}%", pairStart.first, pairStart.second, textPaint)
            canvas.drawText(it.category, pairStart.first - 50, pairStart.second + 30, textPaint)

        }
    }

    override fun showExpenseByCategory(expenseByCategory: List<ExpenseByCategory>) {
        var startAngle = 0f
        for ((index, item) in expenseByCategory.reversed().withIndex()) {
            val period: Float = 360f * item.percent / 100
            val paint = paintList.painList()[index].apply { strokeWidth = STROKE_WIDTH }
            listArcParameters.add(
                ParametersDraw(
                    startAngle,
                    period,
                    false,
                    paint,
                    item.percent,
                    item.category
                )
            )
            listAngle.add(Pair(startAngle..(startAngle + period), item.category))//Список категорий и углов, которые они занимают
            startAngle += period
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            when {
                isTouchInArc(event) -> {
                    _expensePresenter.onClickCategory(selectCategory(event))
                }
                else -> {
                    _expensePresenter.onClickAll()
                }
            }
        }
        return true
    }

    private fun isTouchInArc(event: MotionEvent): Boolean {
        val rangeCircleX = (left - STROKE_WIDTH / 2)..(right + STROKE_WIDTH / 2)
        val rangeCircleY = (top - STROKE_WIDTH / 2)..(bottom + STROKE_WIDTH / 2)
        if ((event.x in rangeCircleX) && (event.y in rangeCircleY)) {//произошло нажатие в пределах диаграммы
            return true
        }
        return false
    }

    private fun selectCategory(event: MotionEvent): String {
        val coordX = event.x - centerX
        val coordY = event.y - centerY
        var angle = 180 / PI * atan2(coordY, coordX)//Нахождение угла
        if (angle < 0) {//уход от отрицательного угла
            angle += 360
        }
        return listAngle.first { angle in it.first }.second
    }

    private fun showLegend(parametersDraw: ParametersDraw): Pair<Float, Float> {
        val angleX = cos((parametersDraw.startAngle + parametersDraw.sweepAngle / 2) * PI / 180)//Середина сегтора по оси Х
        val angleY = sin((parametersDraw.startAngle + parametersDraw.sweepAngle / 2) * PI / 180)//Середина сектора по оси Y

        val offsetX: Float = offsetCoord(angleX)
        val coordX = centerX + offsetX * angleX
        val coordY = centerY + radius * angleY
        return Pair(coordX.toFloat(), coordY.toFloat())

    }

    private fun offsetCoord(angleX: Double) =
        if (angleX < 0) {//Чтобы подписи казались на одном радиусе
            radius + 80
        } else {
            radius
        }


}