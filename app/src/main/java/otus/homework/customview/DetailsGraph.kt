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


    private var rectF: RectF? = null
    private var vWidth = 400
    private var vHeight = 300
    private var maxAmount = 0
    private var minDay = 0
    private var maxDay = 0
    private var itemWidth = 0f
    private var heightCoef = 1f
    private var padWidth = 4f

    private var topPadding = 80f

    private var graphColor = Color.CYAN
    private var borderColor = Color.BLACK

    private val paintBorder = Paint().apply {
        color = borderColor
        strokeWidth = padWidth
        style = Paint.Style.STROKE
        textSize = 0F
    }

    private val paintText = Paint().apply {
        color = Color.DKGRAY
        strokeWidth = 0f
        style = Paint.Style.FILL
        textSize = 40f
        textAlignment = TEXT_ALIGNMENT_CENTER
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
                //  оставляем дефолную ширину и высоту
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
                //  оставляем дефолную ширину и высоту
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

        itemWidth = ceil((vWidth - (3 * padWidth)) / (maxDay - minDay + 1).toFloat())

        if (maxAmount > 0) {
            heightCoef = (vHeight - topPadding) / maxAmount.toFloat()
        }
        setMeasuredDimension(vWidth, vHeight)
        this.rectF = RectF(padWidth, padWidth, vWidth - padWidth, vHeight - padWidth)
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
        var x = padWidth * 1.5f
        for (day in minDay..maxDay) {
            if (itemsMap.containsKey(day)) {
                top = (vHeight - itemsMap.getValue(day) * heightCoef).toFloat()
                canvas?.drawRect(x, top, x + itemWidth - 1f, vHeight - padWidth - 1, paint)
                canvas?.drawText(
                    itemsMap.getValue(day).toString(),
                    x + 10,
                    top - 10,
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
            val day = expence.time / (60 * 60 * 24)
            if (!itemsMap.containsKey(day)) {
                itemsMap.put(day, expence.amount)
            } else {
                itemsMap[day] = itemsMap[day] ?: 0 + expence.amount
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
        setColor(detailsGraphState?.color?: Color.RED)
        itemsMap = detailsGraphState?.itemsMap ?: mutableMapOf()
        //requestLayout()
        //invalidate()
    }
}

@Parcelize
class DetailsGraphState(
    val superSavedState: Parcelable?,
    val color: Int,
    val itemsMap: MutableMap<Int, Int>
) : View.BaseSavedState(superSavedState), Parcelable