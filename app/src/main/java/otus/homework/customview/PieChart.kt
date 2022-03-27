package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import java.util.*
import android.graphics.Paint.Style
import android.graphics.RectF

class PieChart(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {
    companion object {
        const val TAG = "PieChart"
    }

    val padWidth = 10f

    private val paint = Paint().apply {
        color = Color.BLACK
        strokeWidth = padWidth
        style = Style.STROKE
    }

    private var oval: RectF? = null
    private var vWidth = 400
    private var vHeight = 300
    private var centerX = 200
    private var centerY = 150
    private var radius = 200

    private var coef = 1f
    private var next=0

    private val list = ArrayList<Int>()

    private val colors = arrayOf(Color.CYAN,
        Color.BLUE,
        Color.MAGENTA,
        Color.GREEN,
        Color.YELLOW,
        Color.DKGRAY,
        Color.RED,
        Color.BLACK,
        Color.LTGRAY,
        Color.WHITE)


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)



        when (widthMode) {
            MeasureSpec.UNSPECIFIED -> {
                setMeasuredDimension(vWidth, vHeight)
            }
            MeasureSpec.AT_MOST -> {
                if (widthSize < vWidth) {
                    vWidth = widthSize
                }
                if (heightSize < vHeight) {
                    vHeight = heightSize
                }
            }
            MeasureSpec.EXACTLY -> {

                vWidth = widthSize
                vHeight = heightSize

                //??? почему здесь нужно это
                super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            }
        }
        setMeasuredDimension(vWidth, vHeight)
        this.oval = RectF(padWidth, padWidth, vWidth - padWidth, vHeight - padWidth)
        var itemSum = 0
        for (item in list) {
            itemSum += item
        }
        coef = 360f / itemSum // сколько занимает одна единица в градусах
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        Log.d(TAG, "onLayout")
    }

    override fun onDraw(canvas: Canvas?) {

        if (this.oval === null) {
            return
        }
        canvas?.drawOval(this.oval!!, paint)

        if (list.size == 0) return

        var startAngle = 0f
        paint.style = Style.FILL
        for (item in list) {
          paint.color = getNextColor() // ) paint.color = Color.MAGENTA else paint.color = Color.GRAY
            canvas?.drawArc(oval!!, startAngle, (coef * item).toFloat(), true, paint)
            startAngle = startAngle + coef * item
        }

        Log.d(TAG, "onDraw")
    }

    fun getNextColor(): Int {
        val color= colors[next]
        next++
        if (next >= colors.size) {
            next = 0
        }
        return color
    }

    fun setValues(values: List<Int>) {
        Log.d(TAG, "setValues")

        list.clear()
        list.addAll(values)

        requestLayout()
        invalidate()
    }
}