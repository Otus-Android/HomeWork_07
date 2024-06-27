package otus.homework.customview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.atan2
import kotlin.math.min

class PieChartView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val dataItemList = getDataItemList(context)
    private val categoryMap = mutableMapOf<String, Int>()

    private var categories = mutableListOf<String>()
    private var amounts = mutableListOf<Int>()

    private val paint = Paint().apply {
        isAntiAlias = true
    }

    var onClickListener: ((String) -> Unit)? = null

    private val colors = listOf(
        Color.argb(255, 246, 156, 156),
        Color.argb(255, 246, 198, 156),
        Color.argb(255, 246, 236, 156),
        Color.argb(255, 207, 246, 156),
        Color.argb(255, 156, 246, 161),
        Color.argb(255, 156, 246, 205),
        Color.argb(255, 156, 241, 246),
        Color.argb(255, 156, 185, 246),
        Color.argb(255, 196, 156, 246),
        Color.argb(255, 246, 156, 225),
    )

    init {
        dataItemList?.forEach { dataItem ->
            val currentAmount = categoryMap[dataItem.category] ?: 0
            categoryMap[dataItem.category] = currentAmount + dataItem.amount
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val size = if (widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY) {
            min(widthSize, heightSize)
        } else {
            resources.displayMetrics.widthPixels
        }

        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas) {
        val radius = (width.coerceAtMost(height) / 2 * 0.8).toFloat()
        val centerX = (width / 2).toFloat()
        val centerY = (height / 2).toFloat()
        var startAngle = 0f

        categories.clear()
        categories.addAll(categoryMap.keys.toList())

        amounts.clear()
        amounts.addAll(categoryMap.values.toList())

        amounts.forEachIndexed { index, value ->
            val sweepAngle = 360f * value / amounts.sum()

            paint.color = try {
                colors[index]
            } catch (ex: ArrayIndexOutOfBoundsException) {
                Color.BLACK
            }

            canvas.drawArc(
                centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius,
                startAngle,
                sweepAngle,
                true,
                paint
            )

            startAngle += sweepAngle
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        return Bundle().apply {
            putIntArray("amounts", amounts.toIntArray())
            putParcelable("superState", super.onSaveInstanceState())
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            super.onRestoreInstanceState(state.getParcelable("superState"))
            amounts.clear()
            amounts.addAll(state.getIntArray("amounts")?.toList() ?: emptyList())
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val x = event.x
            val y = event.y
            val centerX = width / 2f
            val centerY = height / 2f
            var angle =
                Math.toDegrees(atan2(y.toDouble() - centerY, x.toDouble() - centerX)).toFloat()
            var startAngle = 0f

            if (angle <= 0) angle += 360

            for (i in 0 until categories.size) {
                val sweepAngle = 360f * amounts[i] / amounts.sum()

                if (angle >= startAngle && angle < startAngle + sweepAngle) {
                    onClickListener?.invoke(categories[i])
                    return true
                }

                startAngle += sweepAngle
            }
        }
        return super.onTouchEvent(event)
    }
}