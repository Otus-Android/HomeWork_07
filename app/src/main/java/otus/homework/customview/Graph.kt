package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.annotation.AttrRes
import org.threeten.bp.LocalDate
import kotlin.math.roundToInt

private const val BASE_SIZE_DP = 200f
private const val BASE_POINT_SIZE_DP = 4f

class Graph @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, @AttrRes defStyleAttr: Int = 0
): View(context, attrs, defStyleAttr) {

    private val BASE_SIZE_PX = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, BASE_SIZE_DP, context.resources.displayMetrics).roundToInt()
    private val basePointSizePx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, BASE_POINT_SIZE_DP, context.resources.displayMetrics)

    init {

    }



    private var dataPoints = mutableListOf<DataPoint>()

    private data class DataPoint(
        var proportion: Float = 0f,
        var timeProportion: Float = 0f,
        var proportionPx: Float = 0f,
        var timeProportionPx: Float = 0f,
        var dayNum: Long = 0,
        var amount: Int = 0,
    )

    fun setCharges(charges: List<Charge>, startDate: LocalDate, endDate: LocalDate) {
        if (charges.isEmpty()) {
            dataPoints.clear()
            return
        }

        val daysCount = endDate.toEpochDay() - startDate.toEpochDay()

//        val max = charges.maxOf { it.amount }
        val sorted = charges.sortedBy { it.time }
        val minTime = charges.first().time
        val diffTime = charges.last().time - minTime

        val minDate = minTime.fromEpochSecondToLocalDate()

        val dateToPointMap = mutableMapOf<LocalDate, DataPoint>()
        for (charge in charges) {
            val date = charge.time.fromEpochSecondToLocalDate()
//            if (startDate.isBefore(date) || endDate.isAfter(date)) continue

//            val amountProportion = charge.amount.toFloat() / max

            val point: DataPoint = dateToPointMap.get(date) ?: run {
                val dayNum = date.toEpochDay() - startDate.toEpochDay()

                DataPoint(
                    dayNum = dayNum,
                    timeProportion = dayNum.toFloat() / daysCount
                ).apply {
                    dateToPointMap.put(date, this)
                }
            }

            point.amount += charge.amount
        }

        val max = dateToPointMap.values.maxOf { it.amount }

        dataPoints = dateToPointMap.values
            .sortedBy { it.timeProportion }
            .onEach { it.amount.toFloat() / max }
            .toMutableList()

//        dataPoints = sorted.map {
//            DataPoint(
//                it.amount.toFloat() / max,
//                0f,
//                (it.time - minTime).toFloat() / diffTime,
//                0f,
//            ) }.toMutableList()

        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val wMode = MeasureSpec.getMode(widthMeasureSpec)
        val wSize = MeasureSpec.getSize(widthMeasureSpec)
        val hMode = MeasureSpec.getMode(heightMeasureSpec)
        val hSize = MeasureSpec.getSize(heightMeasureSpec)

        val width = View.getDefaultSize(BASE_SIZE_PX, widthMeasureSpec)
        val height = View.getDefaultSize(BASE_SIZE_PX, heightMeasureSpec)

        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        for (dataPoint in dataPoints) {
            dataPoint.proportionPx = h * dataPoint.proportion
            dataPoint.timeProportionPx = w * dataPoint.timeProportion
        }
    }

    private val pointsPaint = Paint().apply {
        this.color = Color.BLACK
        this.strokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3f, context.resources.displayMetrics)
        this.style = Paint.Style.FILL_AND_STROKE
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        var pointX = 0f
        when (dataPoints.size) {
            0 -> Unit
            1 -> {
                val point = dataPoints.first()
                canvas.drawPoint(point.timeProportionPx, point.proportionPx, pointsPaint)
            }
            else -> {
                for (i in 0 until dataPoints.lastIndex) {
                    pointX += dataPoints[i].timeProportionPx

                    canvas.drawLine(pointX, dataPoints[i].proportionPx, pointX + dataPoints[i + 1].timeProportionPx, dataPoints[i + 1].proportionPx, pointsPaint)
                }
            }
        }
    }
}