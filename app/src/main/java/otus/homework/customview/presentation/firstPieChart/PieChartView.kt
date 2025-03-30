package otus.homework.customview.presentation.firstPieChart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.Typeface
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import otus.homework.customview.domain.Category
import otus.homework.customview.presentation.firstPieChart.model.PieData
import otus.homework.customview.presentation.firstPieChart.model.PieSlice
import otus.homework.customview.utils.px
import java.text.NumberFormat
import kotlin.math.cos
import kotlin.math.sin

class PieChartView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
) : View(context, attributeSet) {
    private var pieData: PieData? = null
    private val mockData = PieData(
        totalValue = 100,
        pieSlices = mapOf(
            Category.SPORTS.title to PieSlice(
                categoryTitle = Category.SPORTS.title,
                amount = 25,
                startAngle = 0f,
                sweepAngle = 90f,
                indicatorCircleLocation = PointF(),
                paint = Paint().apply { color = Color.RED }
            ),
            Category.ALCOHOL.title to PieSlice(
                categoryTitle = Category.ALCOHOL.title,
                amount = 25,
                startAngle = 90f,
                sweepAngle = 90f,
                indicatorCircleLocation = PointF(),
                paint = Paint().apply { color = Color.GRAY }
            ),
            Category.TRANSPORT.title to PieSlice(
                categoryTitle = Category.TRANSPORT.title,
                amount = 25,
                startAngle = 180f,
                sweepAngle = 90f,
                indicatorCircleLocation = PointF(),
                paint = Paint().apply { color = Color.BLUE }
            ),
            Category.GROCERIES.title to PieSlice(
                categoryTitle = Category.GROCERIES.title,
                amount = 25,
                startAngle = 270f,
                sweepAngle = 90f,
                indicatorCircleLocation = PointF(),
                paint = Paint().apply { color = Color.GREEN }
            ),
        )
    )
    private val desiredSize = 400.px
    private var center = PointF(0f, 0f)
    private var outerRectangle = RectF(100f, 100f, 600f, 600f)
    private val outerDiameter
        get() =  outerRectangle.width()
    private val outerRadius
        get() = outerDiameter / 2

    private val boarderPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val innerCirclePaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val mainTextPaint = TextPaint().apply {
        color = Color.BLACK
        isAntiAlias = true
        typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
    }

    private val secondTextPaint = TextPaint().apply {
        color = Color.GRAY
        isAntiAlias = true
        typeface = Typeface.SANS_SERIF
    }

    init {
        if (isInEditMode) {
            pieData = mockData
        }
    }

    fun setData(pieData: PieData) {
        this.pieData = pieData

        requestLayout()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val wMode = MeasureSpec.getMode(widthMeasureSpec)
        val wSize = MeasureSpec.getSize(widthMeasureSpec)

        val hMode = MeasureSpec.getMode(heightMeasureSpec)
        val hSize = MeasureSpec.getSize(heightMeasureSpec)

        val viewWidth: Int = when (wMode) {
            MeasureSpec.AT_MOST -> desiredSize.coerceAtMost(wSize)
            MeasureSpec.EXACTLY -> wSize
            MeasureSpec.UNSPECIFIED -> desiredSize
            else -> desiredSize
        }

        val viewHeight: Int = when (hMode) {
            MeasureSpec.AT_MOST -> desiredSize.coerceAtMost(hSize)
            MeasureSpec.EXACTLY -> hSize
            MeasureSpec.UNSPECIFIED -> desiredSize
            else -> desiredSize
        }
        setMeasuredDimension(viewWidth, viewHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        pieData?.let { pieData ->
            canvas.drawSectors(pieData)

            canvas.drawInnerCycleWithTotal(title = pieData.title, totalValue = pieData.totalValue)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        center = PointF(width / 2f, height / 2f)

        setElementsSize()
        setCircleBounds()

    }

    private fun setElementsSize() {
        mainTextPaint.textSize = height / 20f
        secondTextPaint.textSize = height / 25f
        boarderPaint.strokeWidth = height / 90f
    }

    private fun setCircleBounds() {
        outerRectangle.apply {
            top = 0f
            bottom = height.toFloat()
            left = 0f
            right = width.toFloat()
        }
    }

    private fun Canvas.drawSectors(
        pieData: PieData
    ) {
        pieData.pieSlices.forEach {
            drawArc(
                /* oval = */ outerRectangle,
                /* startAngle = */ it.value.startAngle,
                /* sweepAngle = */ it.value.sweepAngle,
                /* useCenter = */ true,
                /* paint = */ it.value.paint
            )
            drawDivider(
                centerX = center.x,
                centerY = center.y,
                radius = outerRadius,
                angle = it.value.startAngle + it.value.sweepAngle,
            )
        }
    }

    private fun Canvas.drawDivider(centerX: Float, centerY: Float, radius: Float, angle: Float) {
        val endX = centerX + radius * cos(Math.toRadians(angle.toDouble())).toFloat()
        println(endX)
        val endY = centerY + radius * sin(Math.toRadians(angle.toDouble())).toFloat()
        println(endX)

        drawLine(centerX, centerY, endX, endY, boarderPaint)
    }

    private fun Canvas.drawInnerCycleWithTotal(
        title: String,
        totalValue: Int,
    ) {
        drawCircle(
            /* cx = */ center.x,
            /* cy = */ center.y,
            /* radius = */ outerRadius / 2,
            /* paint = */ innerCirclePaint
        )

        val formattedString = buildString {
            append(NumberFormat.getInstance().format(totalValue))
            append(" $")
        }
        val valueWidth = mainTextPaint.measureText(formattedString)

        val fontMetrics = mainTextPaint.fontMetrics
        val textBaseline = center.y - (fontMetrics.ascent + fontMetrics.descent) / 2

        drawText(
            /* text = */ formattedString,
            /* start = */ 0,
            /* end = */ formattedString.length,
            /* x = */ center.x - valueWidth / 2,
            /* y = */ textBaseline,
            /* paint = */ mainTextPaint
        )

        val titleWidth = secondTextPaint.measureText(title)
        val titleFontMetrics = mainTextPaint.fontMetrics
        val titleBaseline = textBaseline + (titleFontMetrics.ascent + titleFontMetrics.descent) * 1.5f

        drawText(
            /* text = */ title,
            /* start = */ 0,
            /* end = */ title.length,
            /* x = */ center.x - titleWidth / 2,
            /* y = */ titleBaseline,
            /* paint = */ secondTextPaint
        )
    }
}