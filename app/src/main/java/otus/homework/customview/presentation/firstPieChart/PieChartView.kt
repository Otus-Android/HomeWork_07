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
import otus.homework.customview.presentation.firstPieChart.model.PieData
import otus.homework.customview.utils.mockData
import otus.homework.customview.utils.px
import java.text.NumberFormat
import kotlin.math.cos
import kotlin.math.sin

class PieChartView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
) : View(context, attributeSet) {
    private var pieData: PieData? = null
    private val desiredSize = 400.px
    private var center = PointF(0f, 0f)
    private var sectors: List<DrawingSector>? = null

    private val boarderPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val mainTextPaint = TextPaint().apply {
        color = Color.BLACK
        isAntiAlias = true
        typeface = Typeface.SANS_SERIF
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

        val desiredWidthWithPadding = desiredSize + paddingLeft + paddingRight
        val desiredHeightWithPadding = desiredSize + paddingTop + paddingBottom

        val viewWidth: Int = when (wMode) {
            MeasureSpec.AT_MOST -> desiredWidthWithPadding.coerceAtMost(wSize)
            MeasureSpec.EXACTLY -> wSize
            MeasureSpec.UNSPECIFIED -> desiredWidthWithPadding
            else -> desiredWidthWithPadding
        }

        val viewHeight: Int = when (hMode) {
            MeasureSpec.AT_MOST -> desiredHeightWithPadding.coerceAtMost(hSize)
            MeasureSpec.EXACTLY -> hSize
            MeasureSpec.UNSPECIFIED -> desiredHeightWithPadding
            else -> desiredHeightWithPadding
        }
        setMeasuredDimension(viewWidth, viewHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        pieData?.let { pieData ->
            canvas.drawSectors()
            canvas.drawTextTotal(title = pieData.title, totalValue = pieData.totalValue)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        center = PointF(width / 2f, height / 2f)

        setElementsSize()
        setSectors()
    }

    private fun setElementsSize() {
        mainTextPaint.textSize = height / 20f
        secondTextPaint.textSize = height / 25f
        boarderPaint.strokeWidth = height / 80f
    }

    private fun setSectors() {
        pieData?.let { pieData ->
            val baseRadius = width / 3f
            sectors = pieData.pieSlices.map { pieSlice ->
                val strokeW = 0.3f * baseRadius + (width / 10 * pieSlice.value.thickness)
                val inset = strokeW / 2
                DrawingSector(
                    startAngle = pieSlice.value.startAngle,
                    sweepAngle = pieSlice.value.sweepAngle,
                    paint = pieSlice.value.paint.apply {
                        strokeWidth = strokeW
                        style = Paint.Style.STROKE
                        strokeCap = Paint.Cap.BUTT
                    },
                    rectF = RectF(
                        /* left = */ center.x - baseRadius + paddingLeft.toFloat() - inset,
                        /* top = */ center.y - baseRadius + paddingTop.toFloat() - inset,
                        /* right = */ center.x + baseRadius - paddingRight.toFloat() + inset,
                        /* bottom = */ center.y + baseRadius - paddingBottom.toFloat() + inset
                    )
                )
            }
        }
    }

    private fun Canvas.drawSectors() {
        sectors?.forEach {
            drawArc(
                /* oval = */ it.rectF,
                /* startAngle = */ it.startAngle,
                /* sweepAngle = */ it.sweepAngle,
                /* useCenter = */ false,
                /* paint = */ it.paint
            )
            drawDivider(
                centerX = center.x,
                centerY = center.y,
                radius = width / 2f,
                angle = it.startAngle + it.sweepAngle,
            )
        }
    }

    private fun Canvas.drawDivider(centerX: Float, centerY: Float, radius: Float, angle: Float) {
        val endX = (centerX + 1.1 * radius * cos(Math.toRadians(angle.toDouble()))).toFloat()
        val endY = (centerY + 1.1 * radius * sin(Math.toRadians(angle.toDouble()))).toFloat()

        drawLine(centerX, centerY, endX, endY, boarderPaint)
    }

    private fun Canvas.drawTextTotal(
        title: String,
        totalValue: Int,
    ) {
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

    private data class DrawingSector(
        val startAngle: Float,
        val sweepAngle: Float,
        val paint: Paint,
        val rectF: RectF,
    )
}