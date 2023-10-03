package otus.homework.customview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import java.lang.Exception
import java.lang.Integer.max
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

class PieChart @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet? = null,
) : View(context, attr) {

    companion object {
        private const val BUNDLE_KEY_STATE = "bundle_key_state"
        private const val BUNDLE_KEY_INDEX = "bundle_key_index"
    }

    private var categoryList = emptyList<Category>()

    private val rectangle = RectF()
    private val topPadding = 24
    private val defaultViewSize = 320
    private var total = 1
    private var index = -1

    private val paintInner = Paint()
        .apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }

    private val paintText = Paint()
        .apply {
            color = Color.GRAY
            style = Paint.Style.FILL
            textSize = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                14f,
                resources.displayMetrics
            )
        }

    private val paintList = listOf(
        ContextCompat.getColor(context, R.color.color_0),
        ContextCompat.getColor(context, R.color.color_1),
        ContextCompat.getColor(context, R.color.color_2),
        ContextCompat.getColor(context, R.color.color_3),
        ContextCompat.getColor(context, R.color.color_4),
        ContextCompat.getColor(context, R.color.color_5),
        ContextCompat.getColor(context, R.color.color_6),
        ContextCompat.getColor(context, R.color.color_7),
        ContextCompat.getColor(context, R.color.color_8),
        ContextCompat.getColor(context, R.color.color_9)
    ).map { paintColor ->
        Paint().apply {
            color = paintColor
            style = Paint.Style.FILL
        }
    }

    private var partByCategories = categoryList
        .groupBy { it.category }
        .mapValues { entry -> ((entry.value.sumBy { it.amount }) * 360).toFloat() / total }
        .toList()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthSize = calculateSize(widthMeasureSpec)
        rectangle.apply {
            top = 0f
            bottom = widthSize.toFloat()
            left = 0f
            right = widthSize.toFloat()
        }
        setMeasuredDimension(widthSize, widthSize)
    }

    private fun calculateSize(measureSpec: Int): Int {
        val size = MeasureSpec.getSize(measureSpec)
        return when (MeasureSpec.getMode(measureSpec)) {
            MeasureSpec.UNSPECIFIED -> defaultViewSize
            MeasureSpec.EXACTLY -> size
            MeasureSpec.AT_MOST -> max(defaultViewSize, size)
            else -> throw Exception("MeasureSpec is not implemented")
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        drawSlices(canvas)
        drawCentralCircle(canvas)
        drawText(canvas)
    }

    private fun drawSlices(canvas: Canvas?) {
        var start = 0f
        for ((i, s) in partByCategories.withIndex()) {
            if (i == index - 1) {
                canvas?.drawArc(rectangle, start, s.second, true, paintList[i])
            }
            canvas?.drawArc(
                rectangle.left + topPadding,
                rectangle.top + topPadding,
                rectangle.right - topPadding,
                rectangle.bottom - topPadding,
                start,
                s.second,
                true,
                paintList[i]
            )
            start += s.second
        }
    }

    private fun drawCentralCircle(canvas: Canvas?) {
        with(rectangle) {
            canvas?.drawCircle(
                (top + bottom) / 2,
                (left + right) / 2,
                (right - left) / 3, paintInner
            )
        }
    }

    private fun drawText(canvas: Canvas?) {
        if (index > 0) {
            val text = "%s: %.1f".format(
                partByCategories[index - 1].first,
                partByCategories[index - 1].second
            )
            val textWidth = paintText.measureText(text)
            canvas?.drawText(
                text,
                (rectangle.left + rectangle.right - textWidth) / 2,
                (rectangle.top + rectangle.bottom) / 2,
                paintText
            )
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        bundle.putInt(BUNDLE_KEY_INDEX, index)
        bundle.putParcelable(BUNDLE_KEY_STATE, super.onSaveInstanceState())
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val bundle = state as Bundle
        index = bundle.getInt(BUNDLE_KEY_INDEX)
        super.onRestoreInstanceState(bundle.getParcelable(BUNDLE_KEY_STATE))
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.actionMasked == MotionEvent.ACTION_UP) {
            setIndex(event.x, event.y)
            invalidate()
            requestLayout()
        }
        return true
    }

    private fun setIndex(x: Float, y: Float) {
        var start = 0f
        val centerX = (rectangle.left + rectangle.right) / 2f
        val centerY = (rectangle.bottom + rectangle.top) / 2f
        val angle = getTouchAngle(x, y, centerX, centerY)
        for ((i, s) in partByCategories.withIndex()) {
            if (angle == null) {
                index = -1
            } else if (angle in start..start + s.second) {
                index = if (index != i + 1) i + 1 else -1
            }
            start += s.second
        }
    }

    private fun getTouchAngle(
        touchX: Float,
        touchY: Float,
        centerX: Float,
        centerY: Float
    ): Float? {
        if (!isSlice(touchX, touchY)) return null
        return Math.toDegrees(
            atan2(
                (centerY - touchY).toDouble(),
                (centerX - touchX).toDouble()
            )
        ).toFloat() + 180
    }

    private fun isSlice(touchX: Float, touchY: Float): Boolean {
        val outerRadius = (rectangle.bottom - rectangle.top) / 2f
        val innerRadius = outerRadius / 1.5f
        val centerX = (rectangle.left + rectangle.right) / 2f
        val centerY = (rectangle.bottom + rectangle.top) / 2f
        val realRadius = sqrt((touchX - centerX).pow(2) + (touchY - centerY).pow(2))
        return (realRadius in innerRadius..outerRadius)
    }

    fun setData(categoryList: List<Category>) {
        this.categoryList = categoryList
        total =
            if (this.categoryList.sumOf { it.amount } > 0) this.categoryList.sumOf { it.amount } else 1
        partByCategories = this.categoryList
            .groupBy { it.category }
            .mapValues { entry -> ((entry.value.sumBy { it.amount }) * 360).toFloat() / total }
            .toList()
    }
}