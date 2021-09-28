package otus.homework.customview.piechartview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.RectF
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.util.Range
import android.view.View
import androidx.core.view.GestureDetectorCompat
import kotlinx.parcelize.Parcelize
import otus.homework.customview.R

/**
 *
 *
 * @author Юрий Польщиков on 27.09.2021
 */
class PieChartView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {

    private var padding = 0f
    private val oval = RectF()

    private var data: List<Spending> = emptyList()
    private val angleRanges = ArrayList<Range<Float>>()

    private val categoriesColors = HashMap<Category, Int>()
    private val categoryPaint = Paint()
    private val textPaint = Paint(ANTI_ALIAS_FLAG).apply {
        color = resources.getColor(R.color.black, context.theme)
        textSize = resources.getDimension(R.dimen.pie_chart_text_size)
    }

    // todo брать цвет для центрального круга из атрибутов темы
    private val centerPaint = Paint().apply { color = resources.getColor(R.color.white, context.theme) }
    private var centerRadius = 0f

    private var selectedCategory: Category? = null //todo делать что-то с сектором, по которому произошел клик

    init {
        context.theme.obtainStyledAttributes(
            attributeSet,
            R.styleable.PieChartView,
            0, 0
        ).apply {
            try {
                padding = getDimension(R.styleable.PieChartView_piePadding, 0f)
            } finally {
                recycle()
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)

        val size = when (widthMode) {
            MeasureSpec.EXACTLY -> if (data.isEmpty()) 0 else widthSize
            MeasureSpec.UNSPECIFIED,
            MeasureSpec.AT_MOST -> resources.getDimensionPixelSize(R.dimen.pie_chart_size)
            else -> resources.getDimensionPixelSize(R.dimen.pie_chart_size)
        }
        setMeasuredDimension(size, size)
        oval.apply {
            top = padding
            left = padding
            right = size - padding
            bottom = size - padding
        }
        centerRadius = oval.right / 3
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (i in data.indices) {
            categoryPaint.color = categoriesColors[data[i].category]!!
            canvas.drawArc(
                oval,
                angleRanges[i].lower,
                angleRanges[i].upper - angleRanges[i].lower,
                true,
                categoryPaint
            )
        }
        canvas.drawCircle(oval.centerX(), oval.centerY(), centerRadius, centerPaint)

        // todo пробовал нарисовать проценты рядом с секторами, - сложности с определением координат
        //canvas.drawText("30%", textX.toFloat(), textY.toFloat(), textPaint)

        // вспомогательные линии:
        //canvas.drawLine((width / 2).toFloat(), 0f, (width / 2).toFloat(), height.toFloat(), textPaint)
        //canvas.drawLine(0f, (height / 2).toFloat(), width.toFloat(), (height / 2).toFloat(), textPaint)
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        return PieChartState(superState, selectedCategory)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        (state as? PieChartState)?.let { pieChartState ->
            super.onRestoreInstanceState(pieChartState.superSavedState ?: state)

            selectedCategory = pieChartState.selectedPie
        }
    }

    fun setOnTouchListener(gestureDetector: GestureDetectorCompat, gestureListener: ClickGestureListener<Category>) {
        setOnTouchListener { _, event ->

            val clickAngle = 0f
            // todo как посчитать угол между точкой касания, центром вью и линией между I и IV секторами
            // пробовал через atan(event.y / event.x) * 180 с доп примочками - не получилось
            // нашел способ определять категорию по цвету точки в которую кликнул:
            // https://www.generacodice.com/en/articolo/1690520/click-event-on-pie-chart-in-android-%5Bclosed%5D
            // этот способ все равно не поможет в отрисовке значений процентов рядом с секторами

            Log.d(TAG, "x = ${event.x}, y = ${event.y}, angle = $clickAngle")
            for (i in angleRanges.indices) {
                if (angleRanges[i].contains(clickAngle)) {
                    gestureListener.data = data[i].category
                    break
                }
            }
            gestureDetector.onTouchEvent(event)
        }
    }

    fun setData(data: List<Spending>) {
        this.data = data

        initViewWithData(data)

        requestLayout()
        invalidate()
    }

    private fun initViewWithData(data: List<Spending>) {
        var startAngle = 0f
        for (item in data) {
            if (categoriesColors[item.category] == null) {
                categoriesColors[item.category] = resources.getColor(item.category.color, context.theme)
            }

            val sweepAngle = (360 * item.percent).toFloat()
            angleRanges.add(Range.create(startAngle, startAngle + sweepAngle))

            startAngle += sweepAngle
        }
    }

    @Parcelize
    class PieChartState(
        val superSavedState: Parcelable?,
        val selectedPie: Category?
    ) : View.BaseSavedState(superSavedState), Parcelable

    companion object {
        private const val TAG = "PieChartView"
    }
}
