package otus.homework.customview.customview

import android.content.Context
import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import otus.homework.customview.Outlay
import otus.homework.customview.R
import otus.homework.customview.dp
import otus.homework.customview.getPieChartColors
import kotlin.math.*

class PieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes)  {
    private val sectionColors = context.getPieChartColors()
    private val items = mutableListOf<Outlay>()
    private var pathsSections = listOf<Path>()
    private var amountItems = items.size

    private var startAngle = -90f
    private var sectionAngle = 0f
    private val circlAngle = 360
    private val chartRect = RectF()
    private var centerX = 0f
    private var centerY = 0f

    private var horizontalOffset = 0f
    private var verticalOffset = 0f

    var touchListener : ((Outlay) -> Unit)? = null

    private val paint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    fun insertItems(newItems: List<Outlay>){
        items.clear()
        amountItems = newItems.sumOf { it.amount }
        items.addAll(newItems)
        pathsSections = items.map { Path() }
        requestLayout()
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if(event?.action  == MotionEvent.ACTION_DOWN){
            touchHandle(event)
        }
        return super.onTouchEvent(event)
    }

    private fun touchHandle(event: MotionEvent){
        val rect = RectF()
        val region = Region()
        items.forEachIndexed { index, outlay ->
            pathsSections[index].computeBounds(rect, false)
            region.setPath(pathsSections[index], Region(rect.left.toInt(), rect.top.toInt(), rect.right.toInt(), rect.bottom.toInt())
            )
            if (region.contains(event.x.toInt(), event.y.toInt())) {
                touchListener?.invoke(outlay)
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        var width = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        var height = MeasureSpec.getSize(heightMeasureSpec)

        when (widthMode) {
            MeasureSpec.AT_MOST, MeasureSpec.UNSPECIFIED -> if (width > height) {
                width = height
            }
        }
        when (heightMode) {
            MeasureSpec.AT_MOST, MeasureSpec.UNSPECIFIED -> if (height > width) {
                height = width
            }
        }

        horizontalOffset = if (height > width) 0f else (width - height).toFloat() / 2
        verticalOffset = if (width > height) 0f else (height - width).toFloat() / 2
        chartRect.left = horizontalOffset + paddingLeft
        chartRect.top = verticalOffset + paddingTop
        chartRect.right = width - horizontalOffset - paddingRight
        chartRect.bottom = height - verticalOffset - paddingBottom
        centerX = width.toFloat() / 2
        centerY = height.toFloat() / 2

        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas?) {
        items.forEachIndexed { index, item ->
            sectionAngle = (item.amount.toFloat() / amountItems) * circlAngle
            paint.color = sectionColors[index % 10]
            pathsSections[index].arcTo(
                chartRect,
                startAngle,
                sectionAngle,
                true
            )
            pathsSections[index].lineTo(centerX, centerY)
            pathsSections[index].close()

            canvas?.drawPath(pathsSections[index], paint)
            startAngle += sectionAngle
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        return super.onSaveInstanceState()
    }
}