package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.Shader
import android.os.Parcelable
import android.util.AttributeSet
import android.util.SizeF
import android.view.View
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.min

class LineChartView @JvmOverloads constructor (
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private var mapPurchases : MutableMap<Pair<String, LocalDate>, Long> = mutableMapOf()
    private var mapGraph : MutableMap<LocalDate, Long> = mutableMapOf()
    private var minDate : LocalDate = LocalDate.now()
    private var maxDate : LocalDate = LocalDate.now()
    private var maxAmt : Float = 0f

    private val minHeight = 300.px
    private val minWidth = 300.px
    private var margin = 80

    private lateinit var gradientFill: LinearGradient
    private var viewBoxSize : SizeF = SizeF(0f,0f)
    private var systemPointRadius = 6f
    private var textBound: Rect = Rect()
    private val paintPath = Path()
    private val boldBleuPaint = Paint().apply {
        color = Color.DKGRAY
        strokeWidth = 5f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }
    private var paintLinePoint: Paint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.DKGRAY
        isAntiAlias = true
    }
    private var paintAxis: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        color = Color.BLUE
    }
    private var paintSysPoint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLUE
    }

    private val paintAxisYText = Paint().apply {
        textAlign = Paint.Align.LEFT
        color = Color.BLUE
        textSize = 50f
    }

    private val paintAxisXText = Paint().apply {
        textAlign = Paint.Align.CENTER
        color = Color.BLUE
        textSize = 50f
    }
    private var switchState = false
    private var drawSmoothing = false
    private var drawFillArea = false

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewBoxSize = SizeF(w.toFloat(), h.toFloat())
        gradientFill = LinearGradient(
            viewBoxSize.width,
            viewBoxSize.height,
            viewBoxSize.width,
            0f,
            intArrayOf(Color.MAGENTA, Color.TRANSPARENT),
            null,
            Shader.TileMode.REPEAT
        )
    }

    override fun onDraw(canvas: Canvas) {
        if(mapGraph.isEmpty()) return
        drawAxis(canvas)
        drawGraph(canvas)
    }

    private fun getChartScaleY() = (viewBoxSize.height - 2f*margin.toFloat())/maxAmt
    private fun getChartScaleX() = (viewBoxSize.width - 2f*margin.toFloat())/(mapGraph.count() + 1f )
    private fun getStartDayX() = minDate  //.minusDays(1) value 0
    private fun getEndDayX() = maxDate.plusDays(1)
    private fun getStepsX() = 1f

    private fun drawGraph(canvas: Canvas) {
        // mapGraph
        val scaleY= getChartScaleY()
        val scaleX = getChartScaleX()

        var currentDayX = getStartDayX()
        val lastDayX = getEndDayX()
        var itemStepsX = getStepsX()

        paintPath.reset()
        paintPath.moveTo( margin.toFloat(), viewBoxSize.height - margin.toFloat() )
        while(currentDayX<=lastDayX) {
            val amt = (mapGraph[currentDayX]?:0).toFloat()/100f
            val endX = margin.toFloat() + itemStepsX*scaleX
            val endY = viewBoxSize.height - margin.toFloat() - (amt * scaleY)
            if ( drawSmoothing ) {
                val amtBefore = (mapGraph[currentDayX.minusDays(1)]?:0).toFloat()/100f
                val startX = margin.toFloat() + (itemStepsX-1f)*scaleX
                val startY = viewBoxSize.height - margin.toFloat() - (amtBefore * scaleY)
                val wt = (startX + endX) / 2f

                paintPath.cubicTo(wt, startY, wt, endY, endX, endY)
            } else {
                paintPath.lineTo(endX, endY )
            }

            currentDayX = currentDayX.plusDays(1)
            ++itemStepsX
        }
        if(drawFillArea) {
            paintPath.close()
            boldBleuPaint.style = Paint.Style.FILL
            boldBleuPaint.shader = gradientFill
        } else {
            boldBleuPaint.style = Paint.Style.STROKE
            boldBleuPaint.shader = null
        }
        canvas.drawPath( paintPath, boldBleuPaint )
    }

    private fun drawAxis(canvas: Canvas) {
        canvas.drawLine(
            margin.toFloat(),
            (viewBoxSize.height - margin),
            margin.toFloat(),
            5f,
            paintAxis
        )
        canvas.drawLine(margin.toFloat(), (viewBoxSize.height - margin),
            (viewBoxSize.width - 5f), (viewBoxSize.height - margin), paintAxis)
        canvas.drawCircle(margin.toFloat(),
            (viewBoxSize.height - margin), systemPointRadius, paintSysPoint)

        val stepAxisByY = maxAmt/5f
        val scaleX = getChartScaleX()
        val scaleY= getChartScaleY()
        var currentDayX = getStartDayX()
        val lastDayX = getEndDayX()
        var itemStepsX = getStepsX()


        //point on the x-axis
        paintAxisXText.getTextBounds("X", 0, "X".length, textBound)
        while(currentDayX<=lastDayX) {
            canvas.drawCircle(
                margin + itemStepsX * scaleX,
                viewBoxSize.height - margin,
                systemPointRadius,
                paintSysPoint
            )
            if( currentDayX!=lastDayX )
                canvas.drawText(
                    currentDayX.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    margin + itemStepsX * scaleX,
                    (viewBoxSize.height - margin + 2 * textBound.height()),
                    paintAxisXText
                )
            currentDayX = currentDayX.plusDays(1)
            ++itemStepsX
        }

        // draw y-axis
        paintAxisYText.getTextBounds("0", 0, "0".length, textBound)
        for( i in 1..5) {
            canvas.drawCircle(
                margin.toFloat(),
                viewBoxSize.height - margin.toFloat() - i * stepAxisByY * scaleY,
                systemPointRadius,
                paintSysPoint
            )
            val indexY = String.format( Locale.getDefault(),"%.02f\$",(i * stepAxisByY))
            canvas.drawText(
                indexY,
                margin.toFloat() + textBound.width(),
                viewBoxSize.height - margin.toFloat() - i * stepAxisByY * scaleY + textBound.height() / 2,
                paintAxisYText
            )
        }
    }

    fun setData(receipts: List<ReceiptData>) {
        fun fromDate( times : Long ) = LocalDateTime.ofEpochSecond(times, 0, ZoneOffset.UTC).toLocalDate()
        mapPurchases = receipts.sortedBy { it.time }.groupBy { Pair(it.category, fromDate(it.time ))  }
            .mapValues { (_, records) -> records.sumOf { it.amount } }.toMutableMap()
    }
    fun setSelectedCategory( category: String) {
        mapGraph = mapPurchases.filter {
            it.key.first == category
        }
            .map {
                it.key.second to it.value
            }
            .toMap()
            .toMutableMap()

        minDate = mapGraph.minOf { it.key }
        maxDate = mapGraph.maxOf { it.key }
        maxAmt = mapGraph.maxOf { it.value } / 100f
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val wMode = MeasureSpec.getMode(widthMeasureSpec)
        val hMode = MeasureSpec.getMode(heightMeasureSpec)
        val wSize = MeasureSpec.getSize(widthMeasureSpec)
        val hSize = MeasureSpec.getSize(heightMeasureSpec)

        val measuredWidth = when (wMode) {
            MeasureSpec.EXACTLY -> wSize
            MeasureSpec.AT_MOST -> min(minWidth, wSize)
            MeasureSpec.UNSPECIFIED -> wSize
            else -> minWidth
        }
        val measuredHeight = when (hMode) {
            MeasureSpec.EXACTLY -> hSize
            MeasureSpec.AT_MOST -> min(minHeight, hSize)
            MeasureSpec.UNSPECIFIED -> hSize
            else -> minHeight
        }

        setMeasuredDimension(measuredWidth, measuredHeight)
    }

    fun switchSmoothing() {
        if(switchState)
            drawSmoothing = !drawSmoothing
        else
            drawFillArea = !drawFillArea
        switchState = !switchState
        invalidate()
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        return LineChartState(superState, switchState, drawSmoothing,drawFillArea)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val restoreState = state as? LineChartState
        super.onRestoreInstanceState(restoreState?.superState ?: state)
        switchState = restoreState?.switchState ?: false
        drawSmoothing = restoreState?.drawSmoothing ?: false
        drawFillArea = restoreState?.drawFillArea ?: false
        invalidate()
    }
}

@Parcelize
class LineChartState(private val superSavedState: Parcelable?,
                     val switchState : Boolean,
                     val drawSmoothing: Boolean,
                     val drawFillArea: Boolean) :
    View.BaseSavedState(superSavedState), Parcelable