package otus.homework.canvas

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Point
import android.graphics.PointF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View


class LineGraphView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var myBackgroundColor: Int = Color.rgb(50, 150, 50) // TO_DO xml app:backgroundColor
    private var padding = 50F                                                     // TO_DO xml app:padding
    private var graphData: MutableMap<String, List<LineGraphData>>? = null
    private var scale = 1F
    private var offset = PointF(0F, 0F)
    private var scaleGestureDetector = ScaleGestureDetector(context, MyOnScaleGestureListener())
    private var maxX = 0F
    private var minX = 0F
    private var maxY = 0F
    private var minY = 0F
    private val presetColors: IntArray = context.resources.getIntArray(R.array.presetColors)
    private val textSize = 35F
    private val strokeWidth = 10F
    private var touchPosition: PointF? = null
    private var touchSavedOffset = PointF(offset.x, offset.y)

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        scaleGestureDetector.onTouchEvent(event)
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchSavedOffset = PointF(offset.x, offset.y)
                touchPosition = PointF(event.x, event.y)
                Log.d("***[", "touchPosition=$touchPosition")
                true
            }
            MotionEvent.ACTION_MOVE -> {
                offset.x = touchSavedOffset.x + event.x - touchPosition!!.x
                offset.y = touchSavedOffset.y + event.y - touchPosition!!.y
                invalidate();
                Log.d("***[", "offset=$offset touchPosition=$touchPosition" )
                true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                touchPosition = null
                Log.d("***[", "touchPosition=null")
                true
            }
            else -> {
                // TO?DO Нужно обрабатывать евенты 261 и 262
                // Иначе при двойном касании когда первый и последний пальцы разные, дергается offset.
                // offset резко меняется на разницу позиций пальцев. Пока TO?DO, хотя может это такая фича, а не бага :)
                Log.d("***[", "action=${event.action}")
                false
            }
        }
    }

    inner class MyOnScaleGestureListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            invalidate();
            scale *= detector.scaleFactor
            Log.d("***[", "scale=$scale")
            return true
        }
    }

    fun Int.modToString() = when(this) {
        MeasureSpec.UNSPECIFIED -> "UNSPECIFIED"
        MeasureSpec.EXACTLY->"EXACTLY"
        MeasureSpec.AT_MOST->"AT_MOST"
        else -> "---"
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMod = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMod = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        //Log.d("***[", "widthMod=${widthMod.modToString()} widthSize=$widthSize heightMod=${heightMod.modToString()} heightSize=$heightSize")
        // График подстраивается под размер экрана, по этому size не меняем
        setMeasuredDimension(widthSize,heightSize)
    }

    fun setData(graphData: MutableMap<String, List<LineGraphData>>?) {
        this.graphData = graphData
        invalidate()
        maxX = 0F
        minX = Float.MAX_VALUE
        maxY = 0F
        minY = Float.MAX_VALUE
        offset = PointF(0F, 0F)
        scale = 1F
        graphData ?.let {
            for (g in graphData) {
                graphData[g.key] = g.value.sortedBy { it.valueX }
                for (d in g.value) {
                    maxX = maxOf(maxX, d.valueX)
                    minX = minOf(minX, d.valueX)
                    maxY = maxOf(maxY, d.valueY)
                    minY = minOf(minY, d.valueY)
                }
            }
        }
    }

    val paint = Paint().apply {
        style = Paint.Style.STROKE
        textSize = this@LineGraphView.textSize
        strokeWidth = strokeWidth
    }

    override fun onDrawForeground(canvas: Canvas) {
        canvas.apply {
            drawColor(myBackgroundColor)
            graphData?.let {
                var num = 0
                for (d in it) {
                    showGraph(num++, d.key, d.value)
                    paint.strokeWidth = 2F
                    drawText(d.key, padding + 10, padding + 10 + textSize * 1.3F * num, paint)
                    paint.strokeWidth = strokeWidth
                }
            }
            val path = Path()
            path.moveTo(padding, padding)
            path.lineTo(padding, height - padding)
            path.lineTo(width - padding, height - padding)
            paint.color = Color.BLACK
            drawPath(path, paint)
        }
    }

    private fun LineGraphData.getReX() = (valueX.toFloat() - minX) * (width - 2 * padding) / (maxX - minX) + padding
    private fun LineGraphData.getReY() = height - ((valueY - minY) * (height- 2 * padding) / (maxY - minY) + padding)

    private fun LineGraphData.log(num: Int) { Log.d("***[", "num=$num XY[$valueX,$valueY] -> [${getReX()} ${getReY()}]") }

    private fun Canvas.showGraph(num: Int, category: String, data: List<LineGraphData>) {
        val path = Path()
        if (data.size == 1) {
            path.addCircle(data[0].getReX(), data[0].getReY(), 3F, Path.Direction.CW)
        } else {
            path.moveTo(data[0].getReX(), data[0].getReY())
            //data[0].log(num)
            for (i in 1 until data.size) {
                //data[i].log(num)
                path.lineTo(data[i].getReX(), data[i].getReY())
            }
        }
        val numColor = num % presetColors.size
        paint.color = presetColors[numColor]
        val matrix = Matrix()
        matrix.setScale(scale, scale)
        path.transform(matrix)
        path.offset(offset.x, offset.y)
        drawPath(path, paint)
    }
}