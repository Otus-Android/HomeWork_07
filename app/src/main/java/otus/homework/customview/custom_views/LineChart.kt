package otus.homework.customview.custom_views

import android.content.Context
import android.graphics.*
import android.os.Parcel
import android.os.Parcelable
import android.text.format.DateUtils
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import otus.homework.customview.R
import otus.homework.customview.extensions.getFormattedDate
import otus.homework.customview.extensions.readParcelList
import otus.homework.customview.model.LineChartModel
import otus.homework.customview.model.Payload
import java.util.*
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.roundToInt

class LineChart @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var viewRect = RectF()

    private val chartLineWidth by lazy(LazyThreadSafetyMode.NONE) {
        resources.getDimension(R.dimen.chart_line_width)
    }

    private val gridLineWidth by lazy(LazyThreadSafetyMode.NONE) {
        resources.getDimension(R.dimen.grid_line_width)
    }

    private val axisTextSize by lazy(LazyThreadSafetyMode.NONE) {
        resources.getDimension(R.dimen.axis_text_size)
    }

    private val chartInset by lazy(LazyThreadSafetyMode.NONE) {
        resources.getDimension(R.dimen.chart_inset)
    }

    private val pointRadius by lazy(LazyThreadSafetyMode.NONE) {
        resources.getDimension(R.dimen.point_radius)
    }

    private val backgroundPaint by lazy(LazyThreadSafetyMode.NONE) {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = getColor(R.color.light_gray)
            style = Paint.Style.FILL
        }
    }

    private val chartPaint by lazy(LazyThreadSafetyMode.NONE) {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.RED
            style = Paint.Style.STROKE
            strokeWidth = chartLineWidth
        }
    }

    private val chartPointsPaint by lazy(LazyThreadSafetyMode.NONE) {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }
    }

    private val gridLinesPaint by lazy(LazyThreadSafetyMode.NONE) {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = gridLineWidth
        }
    }

    private val textPaint by lazy(LazyThreadSafetyMode.NONE) {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            style = Paint.Style.FILL
            textSize = axisTextSize
            textAlign = Paint.Align.CENTER
        }
    }

    private val linePath = Path()

    private var widthAxisY = 0f
    private var heightAxisX = 0f

    private var dateItems = mutableSetOf<Long>()

    private var chartData: List<LineChartModel> = emptyList()
        set(value) {
            field = value
            invalidate()
        }

    private val colors by lazy(LazyThreadSafetyMode.NONE) {
        listOf(
            getColor(R.color.first_segment_color),
            getColor(R.color.second_segment_color),
            getColor(R.color.third_segment_color),
            getColor(R.color.fourth_segment_color),
            getColor(R.color.fifth_segment_color),
            getColor(R.color.sixth_segment_color),
            getColor(R.color.seventh_segment_color),
            getColor(R.color.eighth_segment_color),
            getColor(R.color.ninth_segment_color),
            getColor(R.color.tenth_segment_color)
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSpecMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSpecSize = MeasureSpec.getSize(widthMeasureSpec)

        val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)

        val desiredSize = if (heightSpecSize >= widthSpecSize) {
            heightSpecSize * CHART_SIZE_RATIO
        } else {
            widthSpecSize * CHART_SIZE_RATIO
        }

        val width = getMeasuredDimension(
            specMode = widthSpecMode,
            specSize = widthSpecSize,
            desiredSize = desiredSize.roundToInt()
        )
        val height = getMeasuredDimension(
            specMode = heightSpecMode,
            specSize = heightSpecSize,
            desiredSize = desiredSize.roundToInt()
        )

        setMeasuredDimension(width, height)
    }

    private fun getMeasuredDimension(
        specMode: Int,
        specSize: Int,
        desiredSize: Int
    ): Int {
        return when (specMode) {
            MeasureSpec.EXACTLY -> specSize
            MeasureSpec.AT_MOST -> min(desiredSize, specSize)
            else -> desiredSize
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewRect = RectF(0f, 0f, width.toFloat(), height.toFloat())
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.drawRect(viewRect, backgroundPaint)

        val minDate = calculateMinDateValue()
        val maxDate = calculateMaxDateValue()

        val dateInterval = maxDate - minDate
        val dateAxisSignatureCount = (dateInterval / DateUtils.DAY_IN_MILLIS).toInt()

        val xAxisValues = generateSignaturesAxisX(
            minDate = minDate,
            dateAxisSignatureCount = dateAxisSignatureCount
        )

        val maxPrice = calculateMaxPriceValue()

        val priceScaleInterval = (maxPrice / PRICE_GRID_INTERVAL_COUNT).roundUpThousand()
        val yValues = generateSignatureAxisY(priceScaleInterval)

        calculateYAxisWidth(yValues)
        calculateXAxisHeight(xAxisValues)

        val dateScaleInterval =
            (width.toFloat() - chartInset * 2 - widthAxisY) / dateAxisSignatureCount

        val dateUnit = (width.toFloat() - chartInset * 2 - widthAxisY) / dateInterval
        val priceUnit = (height.toFloat() - chartInset * 2 - heightAxisX) / (priceScaleInterval * 5)

        canvas?.drawAxisX(
            axisValues = xAxisValues,
            dateScale = dateScaleInterval
        )
        canvas?.drawAxisY(
            axisValues = yValues,
            priceUnit = priceUnit,
            priceScale = priceScaleInterval
        )
        canvas?.drawHorizontalGridLines()
        canvas?.drawVerticalGridLines(
            dateAxisSignatureCount = dateAxisSignatureCount,
            dateScaleInterval = dateScaleInterval
        )
        canvas?.drawChartLines(
            minDate = minDate,
            dateUnit = dateUnit,
            priceUnit = priceUnit
        )
    }

    fun setLineChartData(payloads: List<Payload>) {
        val payloadsGroup = payloads.groupBy { it.category }

        val resultList = mutableListOf<LineChartModel>()
        payloadsGroup.forEach { (category, payloads) ->
            val model = LineChartModel(
                category = category,
                entities = payloads
                    .groupBy {
                        it.getDayMillis()
                    }
                    .map { (date, localPayloads) ->
                        val totalSum = localPayloads.sumOf { it.amount }
                        dateItems.add(date)
                        Pair(date, totalSum)
                    },
                color = getColorByIndex(payloadsGroup.keys.indexOf(category))
            )
            resultList.add(model)
        }
        chartData = resultList.toList()
    }

    // ============================= DRAW_ELEMENTS ============================
    private fun Canvas.drawAxisY(
        axisValues: List<String>,
        priceUnit: Float,
        priceScale: Int
    ) {
        axisValues.forEachIndexed { index, value ->
            val textBounds = Rect()
            textPaint.getTextBounds(value, 0, value.length, textBounds)

            val textX = viewRect.width() - widthAxisY / 2
            val textY = (viewRect.height() - chartInset - heightAxisX) +
                    textBounds.height() / 2 - (index * priceUnit * priceScale)

            drawText(value, textX, textY, textPaint)
        }
    }

    private fun Canvas.drawAxisX(
        axisValues: List<String>,
        dateScale: Float
    ) {
        axisValues.forEachIndexed { index, value ->
            val textBounds = Rect()
            textPaint.getTextBounds(value, 0, value.length, textBounds)
            val textWidth = textPaint.measureText(value)

            val minPositionX = textWidth / 2
            val textX = (chartInset + index * dateScale).coerceAtLeast(minPositionX)
            val textY = viewRect.height()

            drawText(value, textX, textY, textPaint)
        }
    }

    private fun Canvas.drawHorizontalGridLines() {
        var horizontalGridLineY = chartInset
        val priceInterval =
            (height.toFloat() - chartInset * 2 - heightAxisX) / PRICE_GRID_INTERVAL_COUNT

        repeat(PRICE_GRID_INTERVAL_COUNT + 1) {
            drawLine(
                0f, horizontalGridLineY,
                width.toFloat() - widthAxisY, horizontalGridLineY,
                gridLinesPaint
            )
            horizontalGridLineY += priceInterval
        }
    }

    private fun Canvas.drawVerticalGridLines(
        dateAxisSignatureCount: Int,
        dateScaleInterval: Float
    ) {
        var verticalGridLineX = chartInset

        repeat(dateAxisSignatureCount + 1) {
            drawLine(
                verticalGridLineX, 0f,
                verticalGridLineX, height.toFloat() - heightAxisX,
                gridLinesPaint
            )
            verticalGridLineX += dateScaleInterval
        }
    }

    private fun Canvas.drawChartLines(
        minDate: Long,
        dateUnit: Float,
        priceUnit: Float
    ) {
        chartData.forEach { chartModel ->
            linePath.reset()
            val pointsList = mutableListOf<PointF>()
            chartModel.entities.forEach {
                val pointX = chartInset + (it.first - minDate) * dateUnit
                val pointY = height.toFloat() - chartInset - heightAxisX - it.second * priceUnit

                pointsList.add(
                    PointF(pointX, pointY)
                )
                drawCircle(
                    pointX,
                    pointY,
                    pointRadius,
                    chartPointsPaint.apply {
                        color = chartModel.color
                    }
                )
            }

            if (pointsList.isNotEmpty()) {
                linePath.moveTo(pointsList.first().x, pointsList.first().y)

                for (i in 1 until pointsList.count()) {
                    linePath.lineTo(pointsList[i].x, pointsList[i].y)
                }
            }
            drawPath(
                linePath,
                chartPaint.apply { color = chartModel.color }
            )
        }
    }

    // ======================== GENERATE_AXIS_SIGNATURES ======================
    private fun generateSignaturesAxisX(
        minDate: Long,
        dateAxisSignatureCount: Int
    ): List<String> {
        return buildList {
            for (i in 0..dateAxisSignatureCount) {
                val calculatedDate = minDate + DateUtils.DAY_IN_MILLIS * i
                val date = Date(calculatedDate)
                add(
                    date.getFormattedDate(DATE_AXIS_PATTERN)
                )
            }
        }
    }

    private fun generateSignatureAxisY(
        priceScaleInterval: Int
    ): List<String> {
        return buildList {
            for (i in 0..PRICE_GRID_INTERVAL_COUNT) {
                add(
                    (priceScaleInterval * i).toString()
                )
            }
        }
    }

    // ======================= CALCULATE_AXIS_DIMENSIONS ======================
    private fun calculateYAxisWidth(axisValues: List<String>) {
        axisValues.forEach { value ->
            val textBounds = Rect()
            textPaint.getTextBounds(value, 0, value.length, textBounds)
            val textWidth = textPaint.measureText(value)

            if (textWidth > widthAxisY) {
                widthAxisY = textWidth
            }
        }
    }

    private fun calculateXAxisHeight(axisValues: List<String>) {
        axisValues.forEach { value ->
            val textBounds = Rect()
            textPaint.getTextBounds(value, 0, value.length, textBounds)
            val textHeight = textBounds.height()

            if (textHeight > heightAxisX) {
                heightAxisX = textHeight.toFloat()
            }
        }
    }

    // ====================== DETERMINE_EXTREME_VALUES ========================
    private fun calculateMinDateValue(): Long {
        return chartData.minOfOrNull { model ->
            model.entities.minOfOrNull { it.first } ?: 0L
        } ?: 0L
    }

    private fun calculateMaxDateValue(): Long {
        return chartData.maxOfOrNull { model ->
            model.entities.maxOfOrNull { it.first } ?: 0L
        } ?: 0L
    }

    private fun calculateMaxPriceValue(): Int {
        return chartData.maxOfOrNull { model ->
            model.entities.maxOfOrNull { it.second } ?: 0
        } ?: 0
    }

    // ========================= AUXILIARY_METHODS  ===========================
    private fun Int.roundUpThousand(): Int {
        return ceil(this.toFloat() / 1000).toInt() * 1000
    }

    private fun getColorByIndex(index: Int): Int {
        return colors.getOrNull(index) ?: colors.first()
    }

    private fun getColor(@ColorRes colorRes: Int): Int {
        return ContextCompat.getColor(context, colorRes)
    }

    // ========================== SAVED_RESTORE_STATE ==============================
    override fun onSaveInstanceState(): Parcelable {
        return SavedState(super.onSaveInstanceState())
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        when (state) {
            is SavedState -> {
                super.onRestoreInstanceState(state.superState)
                chartData = state.chartModelList
            }
            else -> super.onRestoreInstanceState(state)
        }
    }

    private inner class SavedState : BaseSavedState {
        var chartModelList = emptyList<LineChartModel>()

        constructor(source: Parcelable?) : super(source) {
            chartModelList = chartData
        }

        constructor(source: Parcel?) : super(source) {
            source?.readParcelList(chartModelList)
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeList(chartModelList)
        }

        @JvmField
        val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {

            override fun createFromParcel(source: Parcel?): SavedState {
                return SavedState(source)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }

    companion object {
        private const val CHART_SIZE_RATIO = 0.3f
        private const val PRICE_GRID_INTERVAL_COUNT = 5
        private const val DATE_AXIS_PATTERN = "dd.MM"
    }

}