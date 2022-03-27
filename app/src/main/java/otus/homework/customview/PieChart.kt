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
import android.os.Parcelable
import android.view.MotionEvent
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.graphics.minus
import kotlinx.parcelize.Parcelize

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

    private var coef = 1f
    private var next = 0

    private var categories: List<Category> = listOf()


    private val colors = arrayOf(
        Color.CYAN,
        Color.BLUE,
        Color.MAGENTA,
        Color.GREEN,
        Color.YELLOW,
        Color.DKGRAY,
        Color.RED,
        Color.BLACK,
        Color.LTGRAY,
        Color.WHITE
    )


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)



        when (widthMode) {
            MeasureSpec.UNSPECIFIED -> {
                //  оставляем дефолную ширину и высоту
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
            }
        }
        centerX = vWidth / 2
        centerY = vHeight / 2
        setMeasuredDimension(vWidth, vHeight)
        this.oval = RectF(padWidth, padWidth, vWidth - padWidth, vHeight - padWidth)

    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        Log.d(TAG, "onLayout")
    }

    override fun onDraw(canvas: Canvas?) {

        if (this.oval === null) {
            return
        }
        paint.style = Style.STROKE
        canvas?.drawOval(this.oval!!, paint)
        Log.d(TAG, "draw canvas")
        if (categories.size == 0) return

        paint.style = Style.FILL
        for (item in categories) {
            paint.color = getNextColor()
            canvas?.drawArc(oval!!, item.startAngle, item.angle, true, paint)
        }

        Log.d(TAG, "onDraw")
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val category = getTouchedCategory(
                event.x,
                event.y
            )
        }
        return true
    }

    fun getTouchedCategory(x: Float, y: Float): Category? {
        var angle = Math.atan((y - centerY) / (x - centerX).toDouble())
        angle = (180 / 3.14) * angle
        if ((x - centerX) < 0) {
            angle = 180 + angle
        } else {
            if ((y - centerY) < 0) {
                angle = 360 + angle
            }
        }

        Log.d(TAG, "угол " + angle.toString())
        return findCategory(angle.toFloat())
    }

    fun findCategory(sAngle: Float): Category? {
        for (category in categories) {
            if ((category.startAngle < sAngle) && ((category.startAngle + category.angle) >= sAngle)) {
                Log.d(TAG, "founded" + category.category)
                return category
            }
        }

        return null


    }

    fun getNextColor(): Int {
        val color = colors[next]
        next++
        if (next >= colors.size) {
            next = 0
        }
        return color
    }

    fun setValues(values: List<Category>) {
        Log.d(TAG, "setValues")

        categories = values

        var itemSum = 0
        for (item in categories) {
            itemSum += item.amount
        }
        coef = 360f / itemSum // сколько занимает одна единица в градусах

        var startAngle = 0f
        for (item in categories) {
            item.startAngle = startAngle
            startAngle = startAngle + coef * item.amount
            item.angle = coef * item.amount
        }

        requestLayout()
        invalidate()
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        return PieState(superState, categories)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val pieState = state as? PieState
        super.onRestoreInstanceState(pieState?.superSavedState ?: state)

        categories = pieState?.categories ?: listOf()
    }
}

@Parcelize
class PieState(
    val superSavedState: Parcelable?,
    val categories: List<Category>
) : View.BaseSavedState(superSavedState), Parcelable