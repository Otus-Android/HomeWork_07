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
    private var rectF = RectF(0f, 0f, 400f, 600f)
    var data: Map<Pair<String, Paint>, Map<Int, Point>> = emptyMap()

    init{
        mPaint.color = Color.BLACK
        mPaint.strokeWidth = 2f
    }

    @JvmName("setData1")
    fun setData(list: Map<Pair<String, Paint>, Map<Int, Point>>) {
        data = list
        invalidate()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val widthAddition = (width.toFloat() - measuredWidth) / 2
        val heightAddition = (height.toFloat() - measuredHeight) / 2
        rectF.let {
            it.left = widthAddition + paddingLeft
            it.top = heightAddition + paddingTop
            it.right = measuredWidth + widthAddition - paddingRight.toFloat()
            it.bottom = measuredHeight + heightAddition - paddingBottom.toFloat()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        when (MeasureSpec.getMode(widthMeasureSpec)) {
            MeasureSpec.UNSPECIFIED,
            MeasureSpec.AT_MOST,
            MeasureSpec.EXACTLY -> {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            }
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val rectF = RectF(0f, 0f, 200f, 200f)
        canvas?.drawLine(2f, 400f, 2f, 0f, mPaint)
        canvas?.drawLine(2f, 400f, 600f, 400f, mPaint)

        var offset = 30f
        data.forEach {cat ->
            val catName = cat.key.first
            val paint = cat.key.second
            paint.strokeWidth = 2f
            mPaint.textSize = 16f
            mPaint.isAntiAlias = true
            canvas?.drawLine(2f,400f + offset, 22f, 400f + offset, paint)
            canvas?.drawText(catName, 44f, 400f + offset, mPaint)
            offset += 30f
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
}