package otus.homework.customview.pieChart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

class PieChart @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var viewWidth: Int = 0
    private var viewHeight: Int = 0

    private var viewSize: Int = 0

    private val path = Path()

    private val paint = Paint().apply {
        flags = Paint.ANTI_ALIAS_FLAG
        style = Paint.Style.STROKE
        color = Color.BLACK
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val w = 1000
        val h = 1000

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        viewWidth = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> min(w, widthSize)
            else -> w
        }

        viewHeight = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> min(h, heightSize)
            else -> h
        }

        viewSize = min(viewWidth, viewHeight)

        setMeasuredDimension(viewSize, viewSize)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cX = (width / 2).toFloat()
        val cY = (height / 2).toFloat()

        val radius = min(width, height) / 2


        val height = 100
        val smallR = radius - height / 2


        path.reset()
        path.moveTo(cX - smallR, cY)
        path.lineTo(cX - radius, cY + height)
        path.quadTo(cX, cY + 2 * height, cX + radius, cY + height)
        path.lineTo(cX + smallR, cY)
        path.quadTo(cX, cY + height, cX - smallR, cY)

        path.close()
        canvas.drawPath(path, paint)
    }

}