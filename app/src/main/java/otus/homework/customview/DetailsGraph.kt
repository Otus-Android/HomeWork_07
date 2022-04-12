package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import kotlinx.parcelize.Parcelize
import java.util.*
import kotlin.math.ceil

class DetailsGraph(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {
    companion object {
        const val TAG = "DetailsGraph"
    }


    private var vWidth = 400
    private var vHeight = 300
    private var maxAmount = 0

    private var minDay = 0
    private var maxDay = 0

    private var secondsPerDay = 60 * 60 * 24
    private var minDaysInGraph = 3

    private var itemWidth = 0f    // ширина столбика
    private var heightCoef = 1f   // кэф для расчета высоты столбика
    private var strokeWidth = 4f  //ширирна обводки
    private var textPadding = 10f //отступ для подписей
    private var rectF: RectF? = null

    private var topPadding = 80f  // отступ сверху чтобы было куда добавлять подписи к столбикам

    private var graphColor = Color.CYAN   // цвет графика по умолчанию
    private var borderColor = Color.BLACK // цвет обводки по умолчанию


    private val paintBorder = Paint().apply {
        color = borderColor
        strokeWidth = strokeWidth
        style = Paint.Style.STROKE
        textSize = 0F
    }

    private val paintText = Paint().apply {
        color = Color.DKGRAY
        strokeWidth = 0f
        style = Paint.Style.FILL
        textSize = 40f
    }

    private val paint = Paint().apply {
        color = graphColor
        strokeWidth = 0f
        style = Paint.Style.FILL
    }

    var itemsMap: MutableMap<Int, Int> = mutableMapOf()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        when (widthMode) {
            MeasureSpec.UNSPECIFIED -> {
                //  оставляем дефолную ширину
            }
            MeasureSpec.AT_MOST -> {
                if (widthSize < vWidth) {
                    vWidth = widthSize
                }
            }
            MeasureSpec.EXACTLY -> {
                vWidth = widthSize
            }
        }
        when (heightMode) {
            MeasureSpec.UNSPECIFIED -> {
                //  оставляем дефолную  высоту
            }
            MeasureSpec.AT_MOST -> {
                if (heightSize < vHeight) {
                    vHeight = heightSize
                }
            }
            MeasureSpec.EXACTLY -> {
                vHeight = heightSize
            }
        }

        minDay = itemsMap.minByOrNull { it.key }?.key ?: 0
        maxDay = itemsMap.maxByOrNull { it.key }?.key ?: 0
        maxAmount = itemsMap.maxByOrNull { it.value }?.value ?: 1

        var daysInGraph = (maxDay - minDay + 1)
        if (daysInGraph < minDaysInGraph) {
            daysInGraph = minDaysInGraph
        }
        itemWidth = ceil((vWidth - (3 * strokeWidth)) / daysInGraph.toFloat())

        if (maxAmount > 0) {
            heightCoef = (vHeight - topPadding) / maxAmount
        }
        setMeasuredDimension(vWidth, vHeight)
        this.rectF = RectF(strokeWidth, strokeWidth, vWidth - strokeWidth, vHeight - strokeWidth)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
    }

    override fun onDraw(canvas: Canvas?) {

        if (this.rectF === null) {
            return
        }

        canvas?.drawRect(this.rectF!!, paintBorder)
        if (itemsMap.size == 0) return

        var top = 0f
        var x = strokeWidth * 1.5f
        for (day in minDay..maxDay) {
            if (itemsMap.containsKey(day)) {
                top = (vHeight - itemsMap.getValue(day) * heightCoef)
                canvas?.drawRect(x, top, x + itemWidth - 1f, vHeight - strokeWidth - 1, paint)
                canvas?.drawText(
                    itemsMap.getValue(day)
                        .toString() + " руб / " + DateUtils.DateFromTimestamp(day * secondsPerDay),
                    x + textPadding,
                    top - textPadding,
                    paintText
                )
            }
            x += itemWidth
        }
    }

    fun setColor(color: Int) {
        this.graphColor = color
        paint.color = color
        invalidate()
    }

    fun setValues(values: List<Expence>) {
        itemsMap.clear()
        for (expence in values) {
            val day = expence.time / secondsPerDay
            if (!itemsMap.containsKey(day)) {
                itemsMap.put(day, expence.amount)
            } else {
                itemsMap[day] = (itemsMap[day] ?: 0) + expence.amount
            }
        }

        requestLayout()
        invalidate()
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        return DetailsGraphState(superState, graphColor, itemsMap)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val detailsGraphState = state as? DetailsGraphState
        super.onRestoreInstanceState(detailsGraphState?.superSavedState ?: state)
        setColor(detailsGraphState?.color ?: Color.RED)
        itemsMap = detailsGraphState?.itemsMap ?: mutableMapOf()
    }
}

@Parcelize
class DetailsGraphState(
    val superSavedState: Parcelable?,
    val color: Int,
    val itemsMap: MutableMap<Int, Int>
) : View.BaseSavedState(superSavedState), Parcelable