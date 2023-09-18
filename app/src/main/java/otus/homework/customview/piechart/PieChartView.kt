package otus.homework.customview.piechart

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.content.withStyledAttributes
import otus.homework.customview.R
import java.util.Random
import kotlin.math.acos
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.properties.Delegates

class PieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var minSize: Int by Delegates.notNull()

    private var centerTextSize: Int by Delegates.notNull()

    @get:ColorInt
    private var centerTextColor: Int by Delegates.notNull()

    private var centerText = ""
    private var centerTextAlpha = 255

    private var pieChartRotation = 0f

    private var onSectionClickListener: ((PieChartSection) -> Unit)? = null

    private val pieChartSectionsPaint = Paint().apply {
        style = Paint.Style.STROKE
        flags = Paint.ANTI_ALIAS_FLAG
    }

    private val textPaint = Paint().apply {
        textAlign = Paint.Align.CENTER
        flags = Paint.ANTI_ALIAS_FLAG
    }

    private var items = emptyList<PieChartItem>()
    private var sections = emptyList<PieChartSection>()

    init {
        readAttrs(context, attrs, defStyleAttr)
    }

    private var size = minSize

    fun setPieChartItems(items: List<PieChartItem>) {
        this.items = items

        calculatePieChartSections()
    }

    fun setCenterText(text: String) {
        this.centerText = text
    }

    fun setOnSectionClickListener(callback: (PieChartSection) -> Unit) {
        onSectionClickListener = callback
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        size = calculateMeasureSize(widthMode, widthSize)
            .coerceAtMost(calculateMeasureSize(heightMode, heightSize))

        pieChartSectionsPaint.strokeWidth = (size / 5).toFloat()

        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawSections(canvas)
        drawCenterText(canvas)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val clickListener = onSectionClickListener
            ?: return super.onTouchEvent(event)

        val touchX = event.x
        val touchY = event.y

        val touchChartPieAngle = getTouchPieChartAngle(touchX, touchY)
        val touchSection = findSectionByAngle(touchChartPieAngle)

        touchSection?.let {
            clickListener.invoke(it)
            invalidate()
        }

        return super.onTouchEvent(event)
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()

        val pieChartSavedState = PieChartSavedState(superState)

        pieChartSavedState.centerText = centerText
        pieChartSavedState.sectionList = sections

        return pieChartSavedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state == null || state !is PieChartSavedState) {
            super.onRestoreInstanceState(state)
            return
        }
        super.onRestoreInstanceState(state.superState)

        centerText = state.centerText ?: this.centerText
        sections = state.sectionList.takeIf { it.isNotEmpty() } ?: this.sections

        invalidate()
    }

    private fun calculatePieChartSections() {
        if (items.isEmpty()) return

        var currentAngle = 0F
        val totalSumAmount = items.sumOf { it.amount }
        val groupedByCategoryItems = items.groupBy { it.category }

        this.sections = groupedByCategoryItems.values.map { categoryItems ->
            val categoryAmountSum = categoryItems.sumOf { it.amount }.toFloat()
            val sweepAngle = categoryAmountSum / totalSumAmount * 360f

            val pieChartSection = PieChartSection(
                categoryItems,
                currentAngle,
                sweepAngle,
                getRandomColor()
            )

            currentAngle += sweepAngle

            return@map pieChartSection
        }
    }

    private fun calculateMeasureSize(mode: Int, size: Int): Int {
        return when (mode) {
            MeasureSpec.UNSPECIFIED -> minSize
            MeasureSpec.EXACTLY,
            MeasureSpec.AT_MOST -> minSize.coerceAtLeast(size)

            else -> throw IllegalStateException("Unsupported MeasureSpec")
        }
    }

    private fun drawSections(canvas: Canvas) {
        val strokeWidth = pieChartSectionsPaint.strokeWidth
        val left = strokeWidth / 2
        val top = strokeWidth / 2
        val right = size.toFloat() - strokeWidth / 2
        val bottom = size.toFloat() - strokeWidth / 2

        sections.forEach { section ->
            val sectionStartAngle = section.startAngle + pieChartRotation
            pieChartSectionsPaint.color = section.color

            canvas.drawArc(
                left, top,
                right, bottom,
                sectionStartAngle,
                section.sweepAngle,
                false,
                pieChartSectionsPaint
            )
        }
    }

    private fun drawCenterText(canvas: Canvas) {
        with(textPaint) {
            alpha = centerTextAlpha
            textSize = centerTextSize.toFloat()
            color = centerTextColor
        }

        canvas.drawText(
            centerText,
            size.toFloat() / 2,
            size.toFloat() / 2 - ((textPaint.descent() + textPaint.ascent()) / 2),
            textPaint
        )
    }

    private fun getTouchPieChartAngle(touchX: Float, touchY: Float): Float? {
        val outerRadius = size / 2
        val innerRadius = outerRadius - pieChartSectionsPaint.strokeWidth
        val centerX = size / 2
        val centerY = size / 2

        val distanceBetweenCenterAndTouchPoint =
            sqrt((centerX - touchX).pow(2) + (centerY - touchY).pow(2))

        if (distanceBetweenCenterAndTouchPoint < innerRadius ||
            distanceBetweenCenterAndTouchPoint > outerRadius
        ) {
            return null
        }

        val zeroVector = PointF(outerRadius.toFloat(), 0f)
        val touchVector = PointF(touchX - centerX, touchY - centerY)

        val vectorMultiply = zeroVector.x * touchVector.x + zeroVector.y * touchVector.y
        val zeroVectorModule = sqrt(zeroVector.x.pow(2) + zeroVector.y.pow(2))
        val touchVectorModule = sqrt(touchVector.x.pow(2) + touchVector.y.pow(2))

        val angleRadians = acos(vectorMultiply / (zeroVectorModule * touchVectorModule))

        val angleBetweenVectors = Math.toDegrees(angleRadians.toDouble()).toFloat()

        return if (touchY < centerY) 360f - angleBetweenVectors else angleBetweenVectors
    }

    private fun findSectionByAngle(angle: Float?): PieChartSection? {
        if (angle == null) return null

        return sections.find {
            it.startAngle < angle && it.startAngle + it.sweepAngle >= angle
        }
    }

    private fun getRandomColor(): Int {
        val rnd = Random()
        return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
    }

    private fun readAttrs(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        context.withStyledAttributes(attrs, R.styleable.PieChartView, defStyleAttr) {
            minSize = getDimensionPixelSize(
                R.styleable.PieChartView_minSize,
                resources.getDimensionPixelSize(R.dimen.pie_chart_min_size_default)

            )
            centerTextSize = getDimensionPixelSize(
                R.styleable.PieChartView_centerTextSize,
                resources.getDimensionPixelSize(R.dimen.pie_chart_text_min_size_default)
            )
            centerTextColor = getColor(
                R.styleable.PieChartView_centerTextColor,
                Color.GREEN
            )

        }
    }

}