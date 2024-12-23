package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import kotlin.math.atan2

class PieChartView @JvmOverloads constructor (
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private var selected : String = ""
    private val data : MutableMap<String, Float> = mutableMapOf()
    private var total = 0f
    private var centerX = 540
    private var centerY = 550
    private var radius = 400
    private val margin = 10
    private lateinit var palette : Palette

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val wMode = MeasureSpec.getMode(widthMeasureSpec)
        val hMode = MeasureSpec.getMode(heightMeasureSpec)
        var wSize = MeasureSpec.getSize(widthMeasureSpec)
        var hSize = MeasureSpec.getSize(heightMeasureSpec)

        when (wMode) {
            MeasureSpec.AT_MOST -> wSize = Integer.min(radius * 2, wSize)
            MeasureSpec.UNSPECIFIED -> wSize = radius * 2
        }
        when (hMode) {
            MeasureSpec.AT_MOST -> hSize = Integer.min(radius * 2, hSize)
            MeasureSpec.UNSPECIFIED -> hSize = radius * 2
        }
        setMeasuredDimension(wSize, hSize)
        radius = Integer.min(wSize, hSize) / 2
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (data.isEmpty()) return

        var angle = 0f
        val paint = Paint()
        val r = (radius - margin).toFloat()
        centerX = canvas.width / 2
        centerY = canvas.height / 2
        val arcRect = RectF(centerX - r, centerY - r, centerX + r, centerY + r)

        for (cat in data) {
            val arc = 360f * cat.value / total
            paint.apply {
                color = palette.getColor(cat.key)
            }
            canvas.drawArc(arcRect, angle, arc, true, paint)
            angle += arc
        }
    }

    fun setContent(products: List<ProductData>, palette : Palette) {
        this.palette = palette
        data.clear()
        val cats = products.groupBy { it.category }
        for (cat in cats) {
            val sum = cat.value.sumOf { it.amount }.toFloat()
            data.put(cat.key, sum)
        }
        total = data.values.sum()
        requestLayout()
        invalidate()
    }

    fun setCategory(s: String) {
        if (s.isNotEmpty()) {
            selected = s
            Toast.makeText(context, s, Toast.LENGTH_SHORT).show()
            invalidate()
        }
    }

    fun pointCategory(x: Float, y: Float): String {
        var category = ""
        val r2 = (x - centerX) * (x - centerX) + (y - centerY) * (y - centerY)
        if (r2 < radius * radius) {
            var angle = 0f
            // convert coordinates
            var point = 180f * atan2(y - centerY,x - centerX) / Math.PI
            if (y < centerY) point += 360f
            // find category sector for point
            for (cat in data) {
                val arc = 360f * cat.value / total
                if (point in angle .. angle + arc) {
                    category = cat.key
                    break
                }
                angle += arc
            }
        }
        return category
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            super.performClick()
        }
        return clickDetector.onTouchEvent(event)
    }

    private val clickDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            setCategory(pointCategory(e.x, e.y))
            return true
        }
    })
}