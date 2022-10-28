package otus.homework.customview.customView

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import otus.homework.customview.models.PiePiece
import otus.homework.customview.models.Point

class GraphView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var mPaint: Paint = Paint()
    var data: Map<Pair<String, Paint>, Map<Int, Point>> = emptyMap()

    init{
        mPaint.color = Color.BLUE
        mPaint.strokeWidth = 2f
    }

    @JvmName("setData1")
    fun setData(list: Map<Pair<String, Paint>, Map<Int, Point>>) {
        data = list
        invalidate()
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val rectF = RectF(0f, 0f, 200f, 200f)
        canvas?.drawLine(2f, 400f, 2f, 0f, mPaint)
        canvas?.drawLine(2f, 400f, 600f, 400f, mPaint)

        data.forEach {cat ->
            val catName = cat.key.first
            val paint = cat.key.second

            var i = 1
            cat.value[i]?.let {
                paint.strokeWidth = 2f
                canvas?.drawLine(2f,400f, it.coorX+2f, 400f-it.coorY, paint)
            }
            cat.value.forEach { point ->
                paint.strokeWidth = 10f
                canvas?.drawPoint(point.value.coorX+2f, 400f-point.value.coorY, paint)
                cat.value[i+1]?.let {
                    paint.strokeWidth = 2f
                    canvas?.drawLine(point.value.coorX+2f, 400f-point.value.coorY, it.coorX+2f, 400f-it.coorY, paint)
                }
                i++
            }

        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return super.onTouchEvent(event)
    }
}