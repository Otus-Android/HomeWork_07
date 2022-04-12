package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.util.AttributeSet
import android.view.View
import android.graphics.Paint.Style
import android.graphics.RectF
import android.os.Parcelable
import android.view.MotionEvent
import kotlinx.parcelize.Parcelize

class PieChart(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {
    companion object {
        const val TAG = "PieChart"
    }

    private var pieChartListener: PieChartTouchListener? = null;

    val strokeWidth = 4f

    private val paintBorder = Paint(ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        strokeWidth = strokeWidth
        style = Style.STROKE
    }

    private val paintSector = Paint(ANTI_ALIAS_FLAG).apply {
        color = Color.CYAN
        strokeWidth = strokeWidth
        style = Style.FILL
    }

    private var oval: RectF? = null
    private var vWidth = 600 // дефолтная ширина
    private var vHeight = 600 //дефолтная высота

    private var centerX = 0 //координаты центра овала
    private var centerY = 0

    private var coef = 1f  //коэф для расчета угла сектора
    private var next = 0   //счетчик цветов

    private var items: List<PieItem> = listOf()

    // возможные цвета
    private val colors = arrayOf(
        Color.rgb(98, 0, 238),

        Color.rgb(3, 218, 197),
        Color.rgb(233, 141, 245),
        Color.rgb(255, 199, 125),
        Color.rgb(199, 0, 110),
        Color.rgb(248, 187, 208),
        Color.rgb(144, 202, 249),
        Color.rgb(55, 0, 179),
        Color.rgb(229, 57, 53),
        Color.rgb(144, 238, 2),
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
            MeasureSpec.AT_MOST -> {
                if (widthSize < vWidth) {
                    vWidth = widthSize
                }
            }
            MeasureSpec.EXACTLY -> {
                vWidth = widthSize
            }
        }
        vHeight = vWidth
        when (heightMode) {
            MeasureSpec.UNSPECIFIED -> {

            }
            MeasureSpec.AT_MOST -> {

                if (heightSize < vHeight) {
                    vHeight = heightSize
                    vWidth = vHeight
                }

            }
            MeasureSpec.EXACTLY -> {
                vHeight = heightSize

            }
        }
        if (vHeight != vWidth) { // если указани размером позволяет, делаем овал кругом
            if ((widthMode != MeasureSpec.EXACTLY) && ((widthMode != MeasureSpec.AT_MOST) || (widthSize > vHeight))) {
                vWidth = vHeight
            }
        }

        centerX = vWidth / 2
        centerY = vHeight / 2
        setMeasuredDimension(vWidth, vHeight)
        this.oval = RectF(strokeWidth, strokeWidth, vWidth - strokeWidth, vHeight - strokeWidth)

    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
    }

    override fun onDraw(canvas: Canvas?) {

        if (this.oval === null) {
            return
        }

        canvas?.drawOval(this.oval!!, paintBorder)
        if (items.size == 0) return

        for (item in items) {
            paintSector.color = item.color
            canvas?.drawArc(oval!!, item.startAngle, item.angle, true, paintSector)
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
        val inEllipse =
            (((x - centerX) * (x - centerX) / (vWidth * vWidth / 4)) + ((y - centerY) * (y - centerY) / (vHeight * vHeight / 4)))
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
            item.color = getNextColor()
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
        next = 0
    }
}

@Parcelize
class PieState(
    val superSavedState: Parcelable?,
    val items: List<PieItem>
) : View.BaseSavedState(superSavedState), Parcelable