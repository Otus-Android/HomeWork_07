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

    private var pieChartListener: PieChartTouchListener? = null;

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

    private var items: List<PieItem> = listOf()


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


    fun setPieChartTouchListener(listener: PieChartTouchListener) {
        this.pieChartListener = listener
    }

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
    }

    override fun onDraw(canvas: Canvas?) {

        if (this.oval === null) {
            return
        }
        paint.style = Style.STROKE
        canvas?.drawOval(this.oval!!, paint)
        if (items.size == 0) return

        paint.style = Style.FILL
        for (item in items) {
            paint.color = getNextColor()
            canvas?.drawArc(oval!!, item.startAngle, item.angle, true, paint)
        }

    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val item = getTouchedItem(
                event.x,
                event.y
            )
            if ((pieChartListener != null) && (item != null)) {
                pieChartListener?.onPieItemClick(item);
            }
        }
        return true
    }

    fun getTouchedItem(x: Float, y: Float): PieItem? {
        val inEllipse = (((x - centerX) * (x - centerX) / (vWidth * vWidth/4)) + ((y - centerY) * (y - centerY) / (vHeight * vHeight/4)))
        if (inEllipse > 1) {
            return null // мы за пределами пайчарта
        }

        var angle = Math.atan((y - centerY) / (x - centerX).toDouble())
        angle = (180 / 3.14) * angle
        if ((x - centerX) < 0) {
            angle = 180 + angle
        } else {
            if ((y - centerY) < 0) {
                angle = 360 + angle
            }
        }

        return findPieItem(angle.toFloat())
    }

    fun findPieItem(sAngle: Float): PieItem? {
        for (item in items) {
            if ((item.startAngle < sAngle) && ((item.startAngle + item.angle) >= sAngle)) {
                return item
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

    fun setValues(values: List<PieItem>) {

        items = values

        var itemSum = 0
        for (item in items) {
            itemSum += item.value
        }
        coef = 360f / itemSum // сколько занимает одна единица в градусах

        var startAngle = 0f
        for (item in items) {
            item.startAngle = startAngle
            startAngle = startAngle + coef * item.value
            item.angle = coef * item.value
        }

        requestLayout()
        invalidate()
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        return PieState(superState, items)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val pieState = state as? PieState
        super.onRestoreInstanceState(pieState?.superSavedState ?: state)

        items = pieState?.items ?: listOf()
    }
}

@Parcelize
class PieState(
    val superSavedState: Parcelable?,
    val items: List<PieItem>
) : View.BaseSavedState(superSavedState), Parcelable