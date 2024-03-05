package otus.homework.customview.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.CornerPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import otus.homework.customview.model.Store
import kotlin.math.max
import kotlin.math.min

class StockChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private val listStore = ArrayList<Store>()
    private val listAmount = ArrayList<Int>()
    private var maxValue = 0
    private var minValue = 0
    private var lastIndex = 0

    private val paint = Paint().apply {
        color = Color.parseColor("#bd7ebe")
        strokeWidth = 8f
        style = Paint.Style.STROKE
        pathEffect = CornerPathEffect(60f)
        textSize = 30f
    }

    private val redPaint = Paint().apply {
        color = Color.parseColor("#b30000")
        style = Paint.Style.FILL
        strokeWidth = 16f
        textSize = 30f
    }

    private val selectPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
        strokeWidth = 16f
        textSize = 60f
    }

    private val path = Path()
    private val upBarsPath = Path()
    private val downBarsPath = Path()

    private var scale = 1f

    private val scaleGestureDetector =
        ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {

            override fun onScale(detector: ScaleGestureDetector): Boolean {
                scale *= detector.scaleFactor
                invalidate()
                return true
            }
        })

    override fun onDraw(canvas: Canvas) {
        val wStep = width.toFloat() / listAmount.size.toFloat()

        val hStep: Float = measuredHeight.toFloat() / (maxValue - minValue).toFloat()

        val newSize = min(listAmount.size, (listAmount.size / scale).toInt())
        val first = max(0, (listAmount.size - newSize) / 2)
        //  val wStep = measuredWidth.toFloat() / newSize.toFloat()

        path.reset()
        path.moveTo(0f, height.toFloat())

        var x = 30f
        var y = 300f
        //var stepText = 100f
        //будем рисовать разное количество точек . при увеличении и уменьшении масштаба.
        listAmount.forEachIndexed { index, item ->

            y = height - ((item - minValue) * hStep)

            path.lineTo(x, y)

            if (lastIndex == index && listStore[index].isSelect) {
                canvas.drawText("${listStore[index].amount}", x, y, selectPaint)
            } else
                canvas.drawText("${listStore[index].amount}", x, y, redPaint)

            x += wStep


            /*   canvas.drawText("График $item , x = $x  y = $y , wStep $wStep width $width, ${listAmount.size}", 10f, stepText, redPaint)
               stepText += 30f*/
        }
        path.lineTo(width.toFloat(), height.toFloat())
        path.close()
        canvas.drawPath(path, paint)
    }

    fun setValues(values: List<Int>, listStoreNew: List<Store> = emptyList()) {
        listAmount.clear()
        listAmount.addAll(values)
        listStore.clear()
        listStore.addAll(listStoreNew)
        maxValue = listAmount.max()
        minValue = listAmount.min()
        requestLayout()
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleGestureDetector.onTouchEvent(event)
        return true
    }

    fun setIndex(index: Int) {
        listStore[index].isSelect = true
        lastIndex = index
        invalidate()
    }
}