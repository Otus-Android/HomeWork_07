package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.annotation.AttrRes
import kotlin.math.roundToInt

private const val BASE_SIZE_DP = 200f
private const val BASE_POINT_SIZE_DP = 4f

class Graph@JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, @AttrRes defStyleAttr: Int = 0
): View(context, attrs, defStyleAttr) {

    private val BASE_SIZE_PX = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, BASE_SIZE_DP, context.resources.displayMetrics).roundToInt()

    init {

    }



    private var dataPoints = mutableListOf<DataPoint>()

    private data class DataPoint(var proportion: Float, var proportionPx: Float)

    fun setCharges(charges: List<Charge>) {
        val max = charges.maxOf { it.amount }
        val sorted = charges.sortedBy { it.time }

        dataPoints = sorted.map { DataPoint(it.amount.toFloat() / max, 0f) }.toMutableList()
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
        }
    }

    private val pointsPaint = Paint().apply {
        this.color = Color.BLACK
        this.strokeWidth = 8f
        this.style = Paint.Style.FILL_AND_STROKE
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for (i in 0 until dataPoints.lastIndex) {
            canvas.drawLine(i * 10f, dataPoints[i].proportionPx, (i + 1) * 10f, dataPoints[i + 1].proportionPx, pointsPaint)
        }
    }
}