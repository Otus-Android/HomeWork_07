package otus.homework.customview

import android.content.Context
import android.graphics.*
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.withTranslation
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
    private var cacheDisplayWidth = context.resources.displayMetrics.widthPixels
    private var cacheDisplayHeight = context.resources.displayMetrics.heightPixels

    private val gridPaint: Paint = Paint()
        .apply {
            isAntiAlias = true
            this.style = Paint.Style.STROKE
            strokeWidth = 1f
            color = Color.GRAY
//            textSize = 17 * resources.displayMetrics.scaledDensity
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

    // какой высоты график
    // какие временные юниты - это секунды, дни, недели?
    // какой масштаб брать max diff (в одной единице измерения)/ available space?
    // какой лейбл рисовать, какой не поместится? какого размера текст, и как расположен
    // рисовать ли грид?
    // непосредственно отрисовка точек - как использовать безье?

    private var tempRectF = PointF()

    private var data: List<ChartDataAccumulated>? = null
    private var plotConfiguration: PlotConfiguration? = null
    private var viewConfiguration: ViewConfiguration? = null
    private var allConfiguration: AllConfiguration? = null

    private var horizontalLegendTextFontSize: Int? = null
    private var verticalLegendTextFontSize: Int? = null
    private var bottomHorizontalTextHeight: Int = 0
    private var leftVerticalTextWidth: Int = 0
    private var minPaddingBetweenLegendText: Int = 0
    private var gridFlag = true


    // to draw text on the vertical left
    fun getTextHeight(text: String, maxWidth: Int, size: Int): Int {
        TODO()
    }

    fun getTextWidth(text: String, maxHeight: Int, size: Int): Int {
        TODO()
    }

    private val ChartData.x
        get() = time
    private val ChartData.y
        get() = amount

    private data class AllConfiguration(
        val viewConfiguration: ViewConfiguration,
        val plotConfiguration: PlotConfiguration,
    ) {
        val xScale: Float =
            viewConfiguration.plotWidth.toFloat() / plotConfiguration.xRange
        val yScale: Float =
            (viewConfiguration.plotHeight * plotYPercentageOfTotalHeight) / plotConfiguration.yRange
        val yOffset: Float =
            viewConfiguration.plotHeight * ((1 - plotYPercentageOfTotalHeight) / 2)


        fun xToScaled(x: Long): Float {
            return (xScale * (x - plotConfiguration.minX).toFloat())
        }

        fun yToScaledInversedCentered(y: Int): Float {
            return (viewConfiguration.plotHeight - (yScale * (y - plotConfiguration.minY) + yOffset))
        }

        fun getCoordinatesWithYInversed(x: Long, y: Int, out: PointF) {
            out.apply {
                this.x = xToScaled(x)
                this.y = yToScaledInversedCentered(y)
            }
        }

        companion object {
            // gravity of plot area is center of view
            const val plotYPercentageOfTotalHeight: Float = 0.5F
        }
    }

    private inner class ViewConfiguration(

    ) {
        val paddingLeft
            get() = getPaddingLeft()
        val paddingTop
            get() = getPaddingTop()

        val plotWidth = width - paddingLeft - paddingRight
        val plotHeight = height - paddingTop - paddingBottom
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

    private fun updateConfiguration() {
        val plotConfiguration = plotConfiguration
        val viewConfiguration = viewConfiguration
        if (plotConfiguration == null || viewConfiguration == null) return
        this.allConfiguration = AllConfiguration(viewConfiguration, plotConfiguration)
    }

    data class ChartDataAccumulated(val date : Date, val amount : Int) {
        val x
            get() = date.time
        val y
            get() = amount
    }

    private val calendar1: Calendar = Calendar.getInstance()
    private val calendar2 = Calendar.getInstance()
    private fun isSameDay(date1: Date, date2: Date): Boolean {
        calendar1.time = date1
        calendar2.time = date2
        return calendar1[Calendar.YEAR] == calendar2[Calendar.YEAR]
                && calendar1[Calendar.MONTH] == calendar2[Calendar.MONTH]
                && calendar1[Calendar.DAY_OF_MONTH] == calendar2[Calendar.DAY_OF_MONTH]
    }
    private fun isSameHour(date1: Date, date2: Date) : Boolean {
        calendar1.time = date1
        calendar2.time = date2
        return calendar1[Calendar.YEAR] == calendar2[Calendar.YEAR]
                && calendar1[Calendar.MONTH] == calendar2[Calendar.MONTH]
                && calendar1[Calendar.DAY_OF_MONTH] == calendar2[Calendar.DAY_OF_MONTH]
                && calendar1[Calendar.HOUR_OF_DAY] == calendar2[Calendar.HOUR_OF_DAY]
    }

    data class ChartDataDated(val chartData: ChartData, val date: Date)

    fun setData(data: List<ChartData>) {
        val sortedData = data.sortedBy { it.time }
        val dataToDates = sortedData.map { ChartDataDated(it, Date(it.time * 1000L)) }
        val aggregated = dataToDates.fold(mutableListOf<MutableList<ChartDataDated>>()) { acc, chartDataDated ->
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
            this.viewConfiguration = ViewConfiguration()
            updateConfiguration()

        }
    }

    var path = Path()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val data = this.data
        if (data == null || data.isEmpty()) return
        val allConfiguration = allConfiguration ?: return
        val height = height
        val width = width

        val plotAreaHeight = allConfiguration.viewConfiguration.plotHeight
        val plotAreaWidth = allConfiguration.viewConfiguration.plotWidth

        canvas.withTranslation(x = paddingStart.toFloat(), y = paddingTop.toFloat()) {
            drawRect(1f, 1f, plotAreaWidth.toFloat() - 1, plotAreaHeight.toFloat() - 1, gridPaint)
            path.reset()
            val firstPoint = data.firstOrNull() ?: return
            allConfiguration.getCoordinatesWithYInversed(firstPoint.x, firstPoint.y, tempRectF)
            drawCircle(tempRectF.x, tempRectF.y, 6f, plotPointsPaint)
            path.moveTo(tempRectF.x, tempRectF.y)
            var index = 1
            var prevX = tempRectF.x
            var prevY = tempRectF.y
            while (index < data.size) {
                val currPoint = data.getOrNull(index) ?: break
                allConfiguration.getCoordinatesWithYInversed(currPoint.x, currPoint.y, tempRectF)
                val currX = tempRectF.x
                val currY = tempRectF.y
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
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)
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
        super.onRestoreInstanceState(state)
    }

    override fun onSaveInstanceState(): Parcelable? {
        return super.onSaveInstanceState()
    }

    // TODO: use bezier
    // TODO: as saved state, can save calculation of point data or settings
    class SavedState : BaseSavedState {
        constructor(source: Parcel?) : super(source)
        constructor(superState: Parcelable?) : super(superState)

        override fun describeContents(): Int {
            TODO("Not yet implemented")
        }

        override fun writeToParcel(dest: Parcel?, flags: Int) {
            TODO("Not yet implemented")
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
