package otus.homework.customview

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.CornerPathEffect
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import androidx.core.view.doOnLayout

private const val MIN_WIDTH_DP = 380
private const val MIN_HEIGHT_DP = 300
private const val SIDES_RATIO = 1.7f
private const val GRID_LINE_STEP_DP = 23
private const val CHART_PADDING_DP = 32
private const val CHART_POINT_RADIUS_DP = 4
private const val SCALE_MARKS_CORNER_RADIUS_DP = 4
class LineChartView : View {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?
    ) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr)


    private val Int.dp: Float
        get() = this * Resources.getSystem().displayMetrics.density
    private val gridLinesPaint: Paint = Paint().apply {
        color = Color.LTGRAY
        strokeWidth = 1.dp
        pathEffect = DashPathEffect(floatArrayOf(2.dp, 1.dp), 0f)
    }
    private val scaleLinesPaint: Paint = Paint().apply {
        color = Color.DKGRAY
        strokeWidth = 2.dp
        strokeCap = Paint.Cap.ROUND
    }
    private val chartLinePaint: Paint = Paint().apply {
        color = Color.RED
        strokeWidth = 2.dp
        style = Paint.Style.STROKE
        pathEffect = CornerPathEffect(4.dp)
        strokeCap = Paint.Cap.ROUND
    }
    private val chartPointPaint: Paint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
    }
    private val categoryNamePaint: Paint = Paint().apply {
        textSize = 16.dp
        color = Color.DKGRAY
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    private val axisMarksPaint: Paint = Paint().apply {
        textSize = 10.dp
        color = Color.DKGRAY
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    }
    private val axisMarksBackgroundFillPaint: Paint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }
    private val axisMarksBackgroundStrokePaint: Paint = Paint().apply {
        color = Color.GRAY
        style = Paint.Style.STROKE
    }
    private var categoryName: String = ""
    private val linePath: Path = Path()
    private val pointPath: Path = Path()
    private val chartAreaRect = RectF()
    private val categoryNameRect = Rect()
    private val axisMarks: MutableList<AxisMark> = mutableListOf()
    private val spendingByTimeData: MutableMap<String, Int> = mutableMapOf()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        // check all available width/height combinations
        when {
            widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST -> {
                setMeasuredDimension(
                    MIN_WIDTH_DP.dp.toInt(),
                    MIN_HEIGHT_DP.dp.toInt()
                )
            }
            widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.AT_MOST -> {
                setMeasuredDimension(
                    widthSize.coerceAtLeast(MIN_WIDTH_DP.dp.toInt()),
                    MIN_HEIGHT_DP.dp.toInt()
                )
            }
            widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.EXACTLY -> {
                setMeasuredDimension(
                    MIN_WIDTH_DP.dp.toInt(),
                    heightSize.coerceAtLeast(MIN_HEIGHT_DP.dp.toInt())
                )
            }
            widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY -> {
                setMeasuredDimension(
                    widthSize.coerceAtLeast(MIN_WIDTH_DP.dp.toInt()),
                    heightSize.coerceAtLeast(MIN_HEIGHT_DP.dp.toInt())
                )
            }
            else -> {
                // nothing to do
            }
        }
    }
    override fun onSaveInstanceState(): Parcelable {
        return LineChartSavedState(super.onSaveInstanceState())
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state !is LineChartView.LineChartSavedState) {
            return super.onRestoreInstanceState(state)
        }
        super.onRestoreInstanceState(state.superState)
        setData(
            categoryName = state.savedCategoryName,
            spendingByTimeData = state.savedSpendingByTimeData
        )
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        // set bounds of the chart
        chartAreaRect.left = CHART_PADDING_DP.dp
        chartAreaRect.top = CHART_PADDING_DP.dp
        chartAreaRect.right = width - CHART_PADDING_DP.dp
        chartAreaRect.bottom = height - CHART_PADDING_DP.dp
    }

    // onDraw() not contained any objects creating or a hard calculations.
    // It only draws prepared data from the class properties only
    override fun onDraw(canvas: Canvas) {

        // draw horizontal grid lines
        var verticalOffset = chartAreaRect.bottom
        while (verticalOffset > chartAreaRect.top) {
            canvas.drawLine(chartAreaRect.left, verticalOffset, chartAreaRect.right, verticalOffset, gridLinesPaint)
            verticalOffset -= GRID_LINE_STEP_DP.dp
        }

        // draw vertical grid lines
        var horizontalOffset = chartAreaRect.left
        while (horizontalOffset < chartAreaRect.right) {
            canvas.drawLine(horizontalOffset, chartAreaRect.top, horizontalOffset, chartAreaRect.bottom, gridLinesPaint)
            horizontalOffset += GRID_LINE_STEP_DP.dp
        }

        // draw horizontal axis
        canvas.drawLine(
            chartAreaRect.left,
            chartAreaRect.bottom,
            chartAreaRect.right,
            chartAreaRect.bottom,
            scaleLinesPaint
        )

        // draw vertical axis
        canvas.drawLine(
            chartAreaRect.left,
            chartAreaRect.bottom,
            chartAreaRect.left,
            chartAreaRect.top,
            scaleLinesPaint
        )

        // draw the chart line
        canvas.drawPath(linePath, chartLinePaint)

        // draw line-chart rounded markers
        canvas.drawPath(pointPath, chartPointPaint)

        // draw the the category name
        canvas.drawText(
            categoryName,
            categoryNameRect.width() / 2f + CHART_PADDING_DP.dp,
            (CHART_PADDING_DP.dp + categoryNameRect.height()) / 2,
            categoryNamePaint
        )

        // draw whether X and Y axis marks
        // depending on class type
        axisMarks.forEach {
            when(it) {
                is AxisMark.AxisXMark -> {
                    canvas.drawText(
                        it.markText,
                        it.markTextXPos,
                        it.markTextYPos,
                        axisMarksPaint
                    )
                }
                is AxisMark.AxisYMark -> {
                    axisMarksPaint.getTextBounds(
                        it.markText,
                        0, it.markText.length,
                        it.rect
                    )
                    val textWidth = it.rect.width() + 4.dp
                    val textHeight = it.rect.height() + 4.dp
                    val textBackgroundLeft = it.markTextXPos
                    val textBackgroundTop = it.markTextYPos - textHeight
                    val textBackgroundRight = it.markTextXPos + textWidth
                    val textBackgroundBottom = it.markTextYPos

                    // draw marks background fill
                    canvas.drawRoundRect(
                        textBackgroundLeft,
                        textBackgroundTop,
                        textBackgroundRight,
                        textBackgroundBottom,
                        SCALE_MARKS_CORNER_RADIUS_DP.dp,
                        SCALE_MARKS_CORNER_RADIUS_DP.dp,
                        axisMarksBackgroundFillPaint
                    )

                    // draw marks background stroke
                    canvas.drawRoundRect(
                        textBackgroundLeft,
                        textBackgroundTop,
                        textBackgroundRight,
                        textBackgroundBottom,
                        SCALE_MARKS_CORNER_RADIUS_DP.dp,
                        SCALE_MARKS_CORNER_RADIUS_DP.dp,
                        axisMarksBackgroundStrokePaint
                    )
                    // draw marks values
                    canvas.drawText(
                        it.markText,
                        it.markTextXPos + textWidth / 2,
                        it.markTextYPos - 3.dp,
                        axisMarksPaint
                    )
                }
            }
        }
    }

    fun setData(
        categoryName: String,
        spendingByTimeData: Map<String, Int>
    ) {
        this.categoryName = categoryName
        this.spendingByTimeData.putAll(spendingByTimeData)
        doOnLayout {
            prepare()
            invalidate()
        }
    }

    //populates path data and markers coordinates
    private fun prepare() {
        categoryNamePaint.getTextBounds(categoryName, 0, categoryName.length, categoryNameRect)
        val maxAmount = spendingByTimeData.maxBy { it.value }.value
        val stepVertical = chartAreaRect.height() / maxAmount
        val stepHorizontal = chartAreaRect.width() / spendingByTimeData.size
        var initHorizontalPos = chartAreaRect.left
        linePath.apply {
            spendingByTimeData.forEach {
                val x = initHorizontalPos
                val y = chartAreaRect.bottom - stepVertical * it.value
                if (x == chartAreaRect.left) moveTo(x, y) else lineTo(x, y)
                initHorizontalPos += stepHorizontal
                axisMarks.add(
                    AxisMark.AxisXMark(
                        markText = it.key,
                        markTextXPos = x,
                        markTextYPos = chartAreaRect.bottom + CHART_PADDING_DP.dp / 2
                    )
                )
                axisMarks.add(
                    AxisMark.AxisYMark(
                        markText = "${it.value} $",
                        markTextXPos = x + 4.dp,
                        markTextYPos = y - 10.dp
                    )
                )
                pointPath.apply {
                    addOval(
                        x - CHART_POINT_RADIUS_DP.dp,
                        y - CHART_POINT_RADIUS_DP.dp,
                        x + CHART_POINT_RADIUS_DP.dp,
                        y + CHART_POINT_RADIUS_DP.dp,
                        Path.Direction.CW
                    )
                }
            }
        }
    }

    // marker sealed interface
    private sealed interface AxisMark {

        // represent X marks
        data class AxisXMark(
            val markText: String,
            val markTextXPos: Float,
            val markTextYPos: Float,
            val rect: Rect = Rect()
        ) : AxisMark

        // represent Y marks
        data class AxisYMark(
            val markText: String,
            val markTextXPos: Float,
            val markTextYPos: Float,
            val rect: Rect = Rect()
        ) : AxisMark
    }

    private inner class LineChartSavedState : BaseSavedState {

        val savedSpendingByTimeData = mutableMapOf<String, Int>()
        var savedCategoryName = categoryName

        constructor(source: Parcelable?) : super(source) {
            savedSpendingByTimeData.putAll(spendingByTimeData)
        }
        private constructor(parcelIn: Parcel) : super(parcelIn) {
            parcelIn.readMap(savedSpendingByTimeData, ClassLoader.getSystemClassLoader())
            savedCategoryName = parcelIn.readString().toString()
        }
        override fun writeToParcel(parcelOut: Parcel, flags: Int) {
            super.writeToParcel(parcelOut, flags)
            parcelOut.writeMap(savedSpendingByTimeData)
            parcelOut.writeString(savedCategoryName)
        }

        @JvmField
        val CREATOR: Parcelable.Creator<LineChartSavedState?> =
            object : Parcelable.Creator<LineChartSavedState?> {
                override fun createFromParcel(parcelIn: Parcel): LineChartSavedState {
                    return LineChartSavedState(parcelIn)
                }

                override fun newArray(size: Int): Array<LineChartSavedState?> {
                    return arrayOfNulls(size)
                }
            }
    }
}