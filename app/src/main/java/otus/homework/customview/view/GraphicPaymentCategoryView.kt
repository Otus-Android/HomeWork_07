package otus.homework.customview.view

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import otus.homework.customview.R
import otus.homework.customview.utils.dpToPx

class GraphicPaymentCategoryView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var legendAlpha = 0f
    private var legendAnimator: ValueAnimator? = null

    data class CategoryPoint(val day: Int, val amount: Float)

    private var categoryData: Map<String, List<CategoryPoint>> = emptyMap()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GRAY
        strokeWidth = 1f
    }
    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GRAY
        textSize = 24f
    }

    private val path = Path()

    private val colorList = ArrayList<Int>(10).apply {
        add(context.getColor(R.color.blue))
        add(context.getColor(R.color.pink_light))
        add(context.getColor(R.color.green))
        add(context.getColor(R.color.purple))
        add(context.getColor(R.color.orange))
        add(context.getColor(R.color.coral))
        add(context.getColor(R.color.purple_light))
        add(context.getColor(R.color.pink))
        add(context.getColor(R.color.blue_green))
        add(context.getColor(R.color.yellow))
    }

    fun setCategoryData(data: Map<String, List<CategoryPoint>>) {
        categoryData = data
        legendAlpha = 0f
        startLegendAnimation()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = DESIRED_WIDTH.dpToPx(resources)
        val desiredHeight = DESIRED_HEIGHT.dpToPx(resources)

        val width = resolveSize(desiredWidth, widthMeasureSpec)

        val height = resolveSize(desiredHeight, heightMeasureSpec)

        setMeasuredDimension(width, height)
    }

    @SuppressLint("DefaultLocale", "DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (categoryData.isEmpty()) return

        val allPoints = categoryData.values.flatten()
        val maxY = (allPoints.maxOfOrNull { it.amount } ?: 0f).coerceAtLeast(1f)
        val minDay = allPoints.minOf { it.day }
        val maxDay = allPoints.maxOf { it.day }

        val chartLeft = 80f
        val chartRight = width.toFloat() - 20f
        val chartTop = 20f
        val chartBottom = height.toFloat() - 60f

        val chartHeight = chartBottom - chartTop
        val chartWidth = chartRight - chartLeft

        val ySteps = 4
        val yStepValue = maxY / ySteps
        for (i in 0..ySteps) {
            val y = chartBottom - i * chartHeight / ySteps
            canvas.drawLine(chartLeft, y, chartRight, y, gridPaint)
            val label = "${(yStepValue * i).toInt()} ₽"
            canvas.drawText(label, 0f, y, axisPaint)
        }

        val totalDays = maxDay - minDay + 1
        for (i in 0 until totalDays step 2) {
            val x = chartLeft + (i.toFloat() / totalDays) * chartWidth
            val label = String.format("%02d", minDay + i)
            canvas.drawText(label, x, height.toFloat() - 10f, axisPaint)
        }

        categoryData.values.forEachIndexed { colorIndex, points ->
            path.reset()
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 6f
            paint.color = colorList[colorIndex % colorList.size]

            points.sortedBy { it.day }.forEachIndexed { index, point ->
                val x = chartLeft + ((point.day - minDay).toFloat() / totalDays) * chartWidth
                val y = chartBottom - (point.amount / maxY) * chartHeight
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            canvas.drawPath(path, paint)
        }
        drawLegendTopRight(canvas)
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        val bundle = Bundle()
        bundle.putParcelable("superState", superState)
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            super.onRestoreInstanceState(state.getParcelable("superState"))
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    private fun startLegendAnimation() {
        legendAnimator?.cancel()
        legendAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1000
            startDelay = 300 // ждём пока график отрисуется
            addUpdateListener {
                legendAlpha = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    private fun drawLegendTopRight(canvas: Canvas) {
        val legendStartY = 10f
        var legendOffsetY = 0f

        categoryData.keys.forEachIndexed { legendColorIndex, category ->
            val color = colorList[legendColorIndex % colorList.size]
            val left = width - 350f
            val top = legendStartY + legendOffsetY
            val size = 32f

            paint.color = color
            paint.style = Paint.Style.FILL
            paint.alpha = (legendAlpha * 255).toInt()
            canvas.drawRect(left, top, left + size, top + size, paint)

            axisPaint.color = Color.GRAY
            axisPaint.textSize = size
            axisPaint.alpha = (legendAlpha * 255).toInt()
            canvas.drawText(category, left + size + 10f, top + size, axisPaint)
            legendOffsetY += 40f
        }
    }

    companion object {
        private const val DESIRED_WIDTH = 600f
        private const val DESIRED_HEIGHT = 400f
    }
}

