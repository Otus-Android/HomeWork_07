package otus.homework.customview

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.withTranslation
import java.text.SimpleDateFormat
import java.util.*

/*

 ╔═════════════════════════════════╗
 ║                                 ║
 ║                                 ║
 ║       ╔═════════════════╗       ║
 ║       ║                 ║       ║
 ║       ║                 ║       ║
 ║       ║                 ║       ║
 ║       ║                 ║       ║
 ║       ║                 ║       ║
 ║       ║                 ║       ║
 ║       ║                 ║       ║
 ║       ║                 ║       ║
 ║       ╚═════════════════╝       ║
 ║                                 ║
 ║ padding zone                    ║
 ╚═════════════════════════════════╝

          <--------------->
             scale zone
 */
class PlotView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {
    private val simpleDateFormatter = SimpleDateFormat.getDateInstance()
    private val gridPaint: Paint = Paint()
        .apply {
            isAntiAlias = true
            this.style = Paint.Style.STROKE
            strokeWidth = 1.5f
            color = Color.GRAY
        }

    private val plotPaint: Paint = Paint()
        .apply {
            isAntiAlias = true
            this.style = Paint.Style.STROKE
            strokeWidth = 4f
            color = Color.BLUE
        }


    private val plotPointsPaint: Paint = Paint()
        .apply {
            isAntiAlias = true
            this.style = Paint.Style.FILL
            strokeWidth = 4f
            color = Color.MAGENTA
        }
    private val xLegendTextPaint: Paint = Paint()
        .apply {
            color = Color.BLACK
            isAntiAlias = true
            textSize = 15f * context.resources.displayMetrics.density
            style = Paint.Style.FILL_AND_STROKE
        }

    private val yLegendTextPaint: Paint = Paint()
        .apply {
            textAlign = Paint.Align.LEFT
            color = Color.BLACK
            isAntiAlias = true
            textSize = 12f * context.resources.displayMetrics.density
            style = Paint.Style.FILL_AND_STROKE
        }
    private val calendar1: Calendar = Calendar.getInstance()
    private val calendar2 = Calendar.getInstance()
    private val fontMetrics = xLegendTextPaint.fontMetrics
    private var cacheDisplayWidth = context.resources.displayMetrics.widthPixels
    private var cacheDisplayHeight = context.resources.displayMetrics.heightPixels
    private var tempPointF = PointF()
    private var data: List<ChartDataAccumulated>? = null
    private var plotConfiguration: PlotConfiguration? = null
    private var viewConfiguration: ViewConfiguration? = null
    private var allConfiguration: AllConfiguration? = null
    private var path = Path()

    fun setData(data: List<ChartData>) {
        val sortedData = data.sortedBy { it.time }
        val dataToDates = sortedData.map { ChartDataDated(it, Date(it.time * 1000L)) }
        val aggregated =
            dataToDates.fold(mutableListOf<MutableList<ChartDataDated>>()) { acc, chartDataDated ->
                val last = acc.lastOrNull()
                val previous = last?.lastOrNull()
                if (previous != null) {
                    calendar1.time = previous.date
                    calendar2.time = chartDataDated.date
                    if (isSameDay(previous.date, chartDataDated.date)) {
                        last.add(chartDataDated)
                    } else {
                        acc.add(mutableListOf(chartDataDated))
                    }
                } else {
                    acc.add(mutableListOf(chartDataDated))
                }
                acc
            }
        val datedAmounts = aggregated.map { aggregatedByDate ->
            val totalAmountSpent = aggregatedByDate.fold(0) { acc, chartDataDateds ->
                acc + chartDataDateds.chartData.amount
            }
            ChartDataAccumulated(date = aggregatedByDate.first().date, totalAmountSpent)
        }

        val minX = datedAmounts.firstOrNull()?.x ?: return
        val maxX = datedAmounts.lastOrNull()?.x ?: return
        val minY = datedAmounts.minByOrNull { it.y }?.y ?: return
        val maxY = datedAmounts.maxByOrNull { it.y }?.y ?: return
        this.data = datedAmounts
        this.plotConfiguration = PlotConfiguration(minX, maxX, minY, maxY)
        updateConfiguration()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (this.viewConfiguration == null) {
            this.viewConfiguration = ViewConfiguration(X_LEGEND_HEIGHT.toInt())
            updateConfiguration()

        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val data = this.data
        if (data == null || data.isEmpty()) return
        val allConfiguration = allConfiguration ?: return
        val plotAreaHeight = allConfiguration.viewConfiguration.plotZoneHeight
        val plotAreaWidth = allConfiguration.viewConfiguration.plotZoneWidth

        canvas.withTranslation(x = paddingStart.toFloat(), y = paddingTop.toFloat()) {
            drawRect(1f, 1f, plotAreaWidth.toFloat() - 1, plotAreaHeight.toFloat() - 1, gridPaint)
            path.reset()
            val firstPoint = data.firstOrNull() ?: return
            allConfiguration.getCoordinatesWithYInversed(firstPoint.x, firstPoint.y, tempPointF)
            // points and bezier
            drawCircle(tempPointF.x, tempPointF.y, 6f, plotPointsPaint)
            path.moveTo(tempPointF.x, tempPointF.y)
            var index = 1
            var prevX = tempPointF.x
            var prevY = tempPointF.y
            while (index < data.size) {
                val currPoint = data.getOrNull(index) ?: break
                allConfiguration.getCoordinatesWithYInversed(currPoint.x, currPoint.y, tempPointF)
                val currX = tempPointF.x
                val currY = tempPointF.y
                path.cubicTo(prevX + (currX - prevX) / 2,
                    prevY,
                    prevX + (currX - prevX) / 2,
                    currY,
                    currX,
                    currY)
                drawCircle(currX, currY, 6f, plotPointsPaint)
                prevX = currX
                prevY = currY
                index++
            }
            canvas.drawPath(path, plotPaint)
            // x legend
            for (i in data.indices) {
                val string = getTextForData(i)
                val topToBaseline = -fontMetrics.top
                when {
                    i == 0 -> {
                        xLegendTextPaint.textAlign = Paint.Align.LEFT
                    }
                    i < data.size - 1 -> {
                        xLegendTextPaint.textAlign = Paint.Align.CENTER
                    }
                    else -> {
                        xLegendTextPaint.textAlign = Paint.Align.RIGHT
                    }
                }
                canvas.drawText(
                    string,
                    0,
                    string.length,
                    allConfiguration.xToScaled(data[i].x),
                    allConfiguration.viewConfiguration.yTopOfXLegend.toFloat() + topToBaseline,
                    xLegendTextPaint
                )
            }
            // y legend
            for (i in data.indices) {
                val string = getYLegendForData(i)
                val topToBaseline = -fontMetrics.top
                val y = allConfiguration.yToScaledInversedCentered(data[i].y)
                canvas.drawLine(0f, y, plotAreaWidth.toFloat(), y, gridPaint)
                canvas.drawText(
                    string,
                    0,
                    string.length,
                    0f,
                    y + topToBaseline,
                    yLegendTextPaint
                )
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val minDisplaySize = cacheDisplayWidth.coerceAtMost(cacheDisplayHeight)
        val resultWidthSize = when (widthMode) {
            MeasureSpec.AT_MOST, MeasureSpec.EXACTLY -> {
                widthSize
            }
            else -> minDisplaySize
        }
        val resultHeightSize = when (heightMode) {
            MeasureSpec.AT_MOST, MeasureSpec.EXACTLY -> {
                heightSize
            }
            else -> minDisplaySize
        }
        setMeasuredDimension(resultWidthSize, resultHeightSize)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val savedState = state as? SavedState
        super.onRestoreInstanceState((state as? BaseSavedState)?.superState)
        if (savedState != null) {
            // do something with savedState here
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        val parcelable = super.onSaveInstanceState()
        return SavedState(parcelable)
    }

    private fun updateConfiguration() {
        val plotConfiguration = plotConfiguration
        val viewConfiguration = viewConfiguration
        if (plotConfiguration == null || viewConfiguration == null) return
        this.allConfiguration = AllConfiguration(viewConfiguration, plotConfiguration)
    }

    private fun getTextForData(index: Int): String {
        return simpleDateFormatter.format(this.data?.getOrNull(index)?.date ?: return "")
    }

    private fun getYLegendForData(index: Int): String {
        return this.data?.getOrNull(index)?.amount?.toString() ?: ""
    }

    private val ChartData.x
        get() = time
    private val ChartData.y
        get() = amount

    private fun isSameDay(date1: Date, date2: Date): Boolean {
        calendar1.time = date1
        calendar2.time = date2
        return calendar1[Calendar.YEAR] == calendar2[Calendar.YEAR]
                && calendar1[Calendar.MONTH] == calendar2[Calendar.MONTH]
                && calendar1[Calendar.DAY_OF_MONTH] == calendar2[Calendar.DAY_OF_MONTH]
    }

    private fun isSameHour(date1: Date, date2: Date): Boolean {
        calendar1.time = date1
        calendar2.time = date2
        return calendar1[Calendar.YEAR] == calendar2[Calendar.YEAR]
                && calendar1[Calendar.MONTH] == calendar2[Calendar.MONTH]
                && calendar1[Calendar.DAY_OF_MONTH] == calendar2[Calendar.DAY_OF_MONTH]
                && calendar1[Calendar.HOUR_OF_DAY] == calendar2[Calendar.HOUR_OF_DAY]
    }

    data class ChartDataDated(val chartData: ChartData, val date: Date)

    data class ChartDataAccumulated(val date: Date, val amount: Int) {
        val x
            get() = date.time
        val y
            get() = amount
    }

    private data class AllConfiguration(
        val viewConfiguration: ViewConfiguration,
        val plotConfiguration: PlotConfiguration,
    ) {
        val xScale: Float =
            viewConfiguration.plotZoneWidth.toFloat() / plotConfiguration.xRange
        val yScale: Float =
            (viewConfiguration.plotZoneHeight * plotYPercentageOfTotalHeight) / plotConfiguration.yRange
        val yOffset: Float =
            viewConfiguration.plotZoneHeight * ((1 - plotYPercentageOfTotalHeight) / 2)

        fun xToScaled(x: Long): Float {
            return (xScale * (x - plotConfiguration.minX).toFloat())
        }

        fun yToScaledInversedCentered(y: Int): Float {
            return (viewConfiguration.plotZoneHeight - (yScale * (y - plotConfiguration.minY) + yOffset))
        }

        fun getCoordinatesWithYInversed(x: Long, y: Int, out: PointF) {
            out.apply {
                this.x = xToScaled(x)
                this.y = yToScaledInversedCentered(y)
            }
        }

        companion object {
            // gravity of plot area is center of view
            const val plotYPercentageOfTotalHeight: Float = 0.7F
        }
    }

    private inner class ViewConfiguration(
        val xLegendHeight: Int,
    ) {
        val paddingLeft
            get() = getPaddingLeft()
        val paddingTop
            get() = getPaddingTop()
        val plotZoneWidth = width - paddingLeft - paddingRight
        val plotZoneHeight = height - paddingTop - paddingBottom - xLegendHeight
        val yTopOfXLegend = plotZoneHeight
    }

    private data class PlotConfiguration(
        val minX: Long,
        val maxX: Long,
        val minY: Int,
        val maxY: Int,
    ) {
        val yRange = maxY - minY
        val xRange = maxX - minX
    }

    class SavedState : BaseSavedState {
        constructor(source: Parcel) : super(source) {

        }
        constructor(superState: Parcelable?) : super(superState)

        override fun describeContents(): Int {
            return 0
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
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

    companion object {
        val X_LEGEND_HEIGHT = 32 * Resources.getSystem().displayMetrics.density
    }
}
