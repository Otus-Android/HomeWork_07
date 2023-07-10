package otus.homework.customview.views

import android.content.Context
import android.graphics.*
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.ColorUtils
import otus.homework.customview.models.ExpenditureCategory
import otus.homework.customview.models.LinearChartPoint
import otus.homework.customview.R
import otus.homework.customview.utils.toPx
import kotlin.math.max
import kotlin.properties.Delegates


class ExpenditureLinearChart(context: Context?, attrs: AttributeSet) :
    View(context, attrs) {

    private val linearChartMinSize = resources.getDimension(R.dimen.linear_chart_min_size).toInt()

    private var columnsCount by Delegates.notNull<Int>()
    private val rowCount = 4

    private val globalRect = Rect()
    private val chartRect = Rect()
    private val bottomSignatureRect = Rect()

    private val chartPath = Path()

    private val gridPaint = Paint().apply {
        color = ColorUtils.setAlphaComponent(Color.GRAY, 128)
        style = Paint.Style.STROKE
        pathEffect = DashPathEffect(floatArrayOf(2f, 4f), 50f)
        strokeWidth = 2f
    }
    private val signaturePaint = Paint().apply {
        color = ColorUtils.setAlphaComponent(Color.GRAY, 192)
        textSize = 24f
    }
    private val mainPaint = Paint().apply {
        pathEffect = CornerPathEffect(40f)
        style = Paint.Style.STROKE
        strokeWidth = 8f
    }

    private lateinit var categories: HashMap<ExpenditureCategory, ArrayList<LinearChartPoint>>
    private var maxValue by Delegates.notNull<Int>()
    private var daysInMonth by Delegates.notNull<Int>()

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        return SavedState(categories, maxValue, daysInMonth, superState)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is SavedState) {
            categories = state.categories
            maxValue = state.maxValue
            daysInMonth = state.daysInMonth
            super.onRestoreInstanceState(state.superState)
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val viewWidth = measureViewWidth(widthMeasureSpec)
        val viewHeight = measureViewWidth(heightMeasureSpec)
        setMeasuredDimension(viewWidth, viewHeight)
    }

    override fun onDraw(canvas: Canvas) {
        calculateRect(canvas)
        measureBackgroundGridColumnsCount()
        drawBackgroundGrid(canvas)
        drawSideSignature(canvas)
        drawBottomSignature(canvas)
        drawLinearChart(canvas)
    }

    fun setupData(
        linearChartPoints: HashMap<ExpenditureCategory, ArrayList<LinearChartPoint>>,
        maxAmount: Int,
        daysInMonth: Int = 29
    ) {
        this.categories = linearChartPoints
        this.maxValue = maxAmount
        this.daysInMonth = daysInMonth
    }

    private fun measureViewWidth(widthMeasureSpec: Int): Int {
        var result = linearChartMinSize
        val specMode = MeasureSpec.getMode(widthMeasureSpec)
        val specSize = MeasureSpec.getSize(widthMeasureSpec)
        when (specMode) {
            MeasureSpec.UNSPECIFIED -> result = linearChartMinSize
            MeasureSpec.AT_MOST, MeasureSpec.EXACTLY -> result = max(linearChartMinSize, specSize)
        }
        return result
    }

    private fun calculateRect(canvas: Canvas) {
        globalRect.set(canvas.clipBounds)

        val signatureHeight = 16f.toPx.toInt()
        val chartHorizontalPadding = 12f.toPx.toInt()
        chartRect.set(
            globalRect.left + chartHorizontalPadding,
            globalRect.top,
            globalRect.right - chartHorizontalPadding,
            globalRect.bottom - signatureHeight
        )
        bottomSignatureRect.set(
            globalRect.left,
            globalRect.bottom - signatureHeight,
            globalRect.right,
            globalRect.bottom
        )
    }

    private fun measureBackgroundGridColumnsCount() {
        val width = chartRect.width()
        columnsCount = when {
            width in (240..359) -> 2
            width in (360..479) -> 3
            width in (480..599) -> 4
            width in (600..719) -> 5
            width in (720..839) -> 6
            width in (840..959) -> 7
            width in (960..1079) -> 8
            width >= 1080 -> 9
            else -> throw IllegalStateException("Width is too small")
        }
    }

    private fun drawBackgroundGrid(canvas: Canvas) {
        canvas.drawRect(chartRect, gridPaint)
        val gridWidth = chartRect.width() / columnsCount
        val gridHeight = chartRect.height() / rowCount
        canvas.save()
        canvas.clipRect(chartRect)
        for (index in 1 until rowCount) {
            canvas.drawLine(
                chartRect.left.toFloat(),
                index * gridHeight.toFloat(),
                chartRect.right.toFloat(),
                index * gridHeight.toFloat(),
                gridPaint
            )
        }
        for (index in 1 until columnsCount) {
            canvas.drawLine(
                chartRect.left + index * gridWidth.toFloat(),
                chartRect.top.toFloat(),
                chartRect.left + index * gridWidth.toFloat(),
                chartRect.bottom.toFloat(),
                gridPaint
            )
        }
        canvas.restore()
    }

    private fun drawSideSignature(canvas: Canvas) {
        val gridHeight = chartRect.height() / rowCount
        for (index in (1..rowCount)) {
            val textToDraw = "${maxValue * (rowCount - index + 1) / rowCount}$"
            val textLength = signaturePaint.measureText(textToDraw)
            val dx = chartRect.right - textLength - 2f.toPx
            val dy = (index - 1) * gridHeight + 10f.toPx
            canvas.drawText(textToDraw, dx, dy, signaturePaint)
        }
    }

    private fun drawBottomSignature(canvas: Canvas) {
        val gridWidth = chartRect.width() / columnsCount
        var dayOfMonth = 1
        val diff = daysInMonth / columnsCount
        for (index in (0..columnsCount)) {
            val textToDraw = dayOfMonth.toString()
            val textLength = signaturePaint.measureText(textToDraw)
            val dx = chartRect.left + index * gridWidth - textLength * .5f
            val dy = bottomSignatureRect.top + 10f.toPx
            canvas.drawText(textToDraw, dx, dy, signaturePaint)

            dayOfMonth += diff
        }
    }

    private fun drawLinearChart(canvas: Canvas) {
        categories.forEach { entry ->
            chartPath.reset()
            entry.value.firstOrNull()?.let {
                val dx = chartRect.left.toFloat()
                val dy = chartRect.top + chartRect.height()
                    .toFloat() * (1 - it.amount / maxValue)
                chartPath.moveTo(dx, dy)
            } ?: chartPath.moveTo(chartRect.left.toFloat(), chartRect.left.toFloat())
            mainPaint.color = entry.key.getChartColor()
            entry.value
                .sortedWith { o1, o2 -> o1.dayInMonth - o2.dayInMonth }
                .forEach {
                    chartPath.apply {
                        val dx = chartRect.left + chartRect.width()
                            .toFloat() * (it.dayInMonth - 1) / (daysInMonth - 1)
                        val dy = chartRect.top + chartRect.height()
                            .toFloat() * (1 - it.amount / maxValue)
                        lineTo(dx, dy)
                    }
                }
            canvas.drawPath(chartPath, mainPaint)
        }
    }

    internal class SavedState : BaseSavedState {

        val categories: HashMap<ExpenditureCategory, ArrayList<LinearChartPoint>>
        val maxValue: Int
        val daysInMonth: Int

        constructor(
            categories: HashMap<ExpenditureCategory, ArrayList<LinearChartPoint>>,
            maxAmount: Int,
            daysInMonth: Int = 29,
            superState: Parcelable?
        ) : super(superState) {
            this.categories = hashMapOf<ExpenditureCategory, ArrayList<LinearChartPoint>>().also {
                it.clear()
                it.putAll(categories)
            }
            this.maxValue = maxAmount
            this.daysInMonth = daysInMonth
        }

        private constructor(input: Parcel) : super(input) {
            categories = readParcelableMap(
                parcel = input,
                kClass = ExpenditureCategory::class.java,
                vClass = LinearChartPoint::class.java
            ) as HashMap<ExpenditureCategory, ArrayList<LinearChartPoint>>
            maxValue = input.readInt()
            daysInMonth = input.readInt()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            writeParcelableMap(out, flags, categories)
            out.writeInt(maxValue)
            out.writeInt(daysInMonth)
        }

        override fun describeContents(): Int {
            return 0
        }

        private fun <K : Parcelable?, V : Parcelable?> writeParcelableMap(
            parcel: Parcel,
            flags: Int,
            map: Map<K, ArrayList<V>>
        ) {
            parcel.writeInt(map.size)
            for ((key, value) in map) {
                parcel.writeParcelable(key, flags)
                parcel.writeList(value)
            }
        }

        // For reading from a Parcel
        private fun <K : Parcelable?, V : Parcelable?> readParcelableMap(
            parcel: Parcel,
            kClass: Class<K>,
            vClass: Class<V>
        ): Map<K, ArrayList<V>> {
            val size = parcel.readInt()
            val map: MutableMap<K, ArrayList<V>> = HashMap(size)
            for (i in 0 until size) {
                val key = kClass.cast(parcel.readParcelable(kClass.classLoader)) as K
                val value = arrayListOf<V>()
                parcel.readList(value, ArrayList::class.java.classLoader)
                map[key] = value
            }
            return map
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState {
                return SavedState(parcel)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }
}