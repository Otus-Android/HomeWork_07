package otus.homework.customview

import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap


class PieChartView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    companion object {
        val colors = arrayListOf(Color.YELLOW, Color.CYAN, Color.GRAY, Color.GREEN, Color.MAGENTA,
            Color.RED, Color.WHITE, Color.LTGRAY, Color.DKGRAY, Color.BLACK)
    }

    private val widthDefault = (200 * context.resources.displayMetrics.density).toInt()
    private val heightDefault = (450 * context.resources.displayMetrics.density).toInt()
    private val padding = (25 * context.resources.displayMetrics.density)
    private val widthRing = 20f * context.resources.displayMetrics.density
    private val paint = Paint().apply {
        color = Color.GRAY
        strokeWidth = widthRing
        style = Paint.Style.STROKE
        flags = Paint.ANTI_ALIAS_FLAG
        textSize = 10 * context.resources.displayMetrics.density
    }
    private var totalWeight = 1
    private val rect = RectF()
    private var categories: LinkedHashMap<String, Category> = LinkedHashMap()
    private var selectedCategory: Category? = null

    var categoryClickedListener: Listener? = null

    interface Listener {
        fun clickCategory(category: Category)
    }

    fun setItems(items: ArrayList<Item>) {
        this.categories.clear()
        items.forEach {
            if (categories.containsKey(it.category)) {
                val category = categories.get(it.category)
                category!!.items?.add(it)
                category.totalAmount = category.totalAmount + it.amount
            } else {
                categories.set(it.category!!, Category(it.category, arrayListOf(it), it.amount, colors.get(categories.size)))
            }
        }
        totalWeight = items.sumOf { it.amount }
        requestLayout()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val modeWidth = MeasureSpec.getMode(widthMeasureSpec)
        val modeHeight = MeasureSpec.getMode(heightMeasureSpec)
        val widthMeasure = MeasureSpec.getSize(widthMeasureSpec)
        val heightMeasure = MeasureSpec.getSize(heightMeasureSpec)
        val width = measureSize(modeWidth, widthMeasure, widthDefault)
        val height = measureSize(modeHeight, heightMeasure, heightDefault)
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas?) {


        val diameter = minOf(width, height)
        val totalWidth = if (width > diameter) {
            diameter
        } else {
            width
        }
        val totalHeight = if (height > diameter) {
            diameter
        } else {
            height
        }
        val paddingLeftRight = (width.toFloat() - diameter + 1) / 2
        val paddingTopBottom = (height.toFloat() - diameter + 1) / 2

        rect.set(
            paddingLeftRight + widthRing / 2 + padding,
            paddingTopBottom + widthRing / 2 + padding,
            totalWidth + paddingLeftRight - widthRing / 2 - padding,
            totalHeight + paddingTopBottom - widthRing / 2 - padding
        )

        paint.color = Color.GRAY
        paint.strokeWidth = widthRing
        paint.style = Paint.Style.STROKE

        var previousAngle = -90F
        categories.forEach {
            val angle = ((it.value.totalAmount.toFloat()/totalWeight.toFloat()) * 360f).toFloat()
            Log.d("PieChartView", "angle $angle previousAngle $previousAngle ${it.value}")
            paint.color = it.value.color
            canvas?.drawArc(rect, previousAngle, angle, false, paint)
            previousAngle += angle
        }


        val centerX = width / 2f
        val centerY = height / 2f
        selectedCategory?.let {
            paint.strokeWidth = 2f * resources.displayMetrics.density
            paint.color = Color.GRAY
            paint.style = Paint.Style.FILL
            val widthText = paint.measureText(it.name)
            canvas?.drawText(it.name!!, centerX - widthText/2, centerY, paint)
        }

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_UP) {
            if (isOnRing(event, rect, widthRing)) {
                val category = clickOnCategory(event)
                selectedCategory = category
                category?.let {
                    categoryClickedListener?.clickCategory(category)
                }
                requestLayout()
                invalidate()
            }
            return true
        }
        return true
    }

    private fun clickOnCategory(event: MotionEvent):Category? {
        var previousAngle = 0F

        categories.forEach {
            val angle = ((it.value.totalAmount.toFloat()/totalWeight.toFloat()) * 360f).toFloat()
            val touchAngle = getAngle(event.x, event.y, rect)
            Log.d("PieChartView", "touchAngle ${touchAngle} ")

            if (previousAngle < touchAngle && (angle + previousAngle) > touchAngle) {
                return it.value
            }
            previousAngle += angle
        }
        return null
    }

    private fun isOnRing(event: MotionEvent, bounds: RectF, strokeWidth: Float): Boolean {
        val distance = distance(
            event.x, event.y,
            bounds.centerX(), bounds.centerY()
        )
        val radius = bounds.width() / 2f
        val halfStrokeWidth = strokeWidth / 2f
        return Math.abs(distance - radius) <= halfStrokeWidth
    }

    private fun getAngle(touchX: Float, touchY: Float, bounds: RectF): Float {

        var angle: Float
        val x2 = touchX - bounds.centerX()
        val y2 = touchY - bounds.centerY()
        val d1 = Math.sqrt((bounds.centerY() * bounds.centerY()).toDouble())
        val d2 = Math.sqrt((x2 * x2 + y2 * y2).toDouble())
        if (touchX >= bounds.centerX()) {
            angle = Math.toDegrees(Math.acos((-bounds.centerY() * y2) / (d1 * d2))).toFloat()
        } else
            angle = 360 - Math.toDegrees(Math.acos((-bounds.centerY() * y2) / (d1 * d2))).toFloat()
        return angle
    }

    private fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return Math.sqrt(Math.pow((x1 - x2).toDouble(), 2.0) + Math.pow((y1 - y2).toDouble(), 2.0))
            .toFloat()
    }

    private fun measureSize(mode: Int, size: Int, sizeMax: Int) = when (mode) {
        MeasureSpec.EXACTLY -> {
            size
        }
        MeasureSpec.AT_MOST -> {
            if (sizeMax <= size) {
                sizeMax
            } else {
                size
            }
        }
        MeasureSpec.UNSPECIFIED -> {
            sizeMax
        }
        else -> {
            size
        }
    }

    override fun onSaveInstanceState(): Parcelable =
        Bundle().apply {
            putParcelable("selectedCategory", selectedCategory)
            putSerializable("categoryes", categories)
            putInt("totalWeight", totalWeight)
            putParcelable("superState", super.onSaveInstanceState())
        }


    override fun onRestoreInstanceState(state: Parcelable?) = super.onRestoreInstanceState(
        if (state is Bundle) {
            selectedCategory = state.getParcelable("selectedCategory")
            categories = state.getSerializable("categoryes") as LinkedHashMap<String, Category>
            totalWeight = state.getInt("totalWeight")
            state.getParcelable("superState")
        } else {
            state
        }
    )
}