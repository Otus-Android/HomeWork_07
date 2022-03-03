package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class GraphicsView(context: Context, attr: AttributeSet) : View(context, attr) {

    private val paint = Paint().apply {
        isAntiAlias = true
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 8f
    }
    private val xyLinesPaint = Paint().apply {
        isAntiAlias = true
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }
    private val amountTextLineYPaint = Paint().apply {
        color = Color.BLACK
        textSize = 40f
        flags = Paint.ANTI_ALIAS_FLAG
        textAlign = Paint.Align.LEFT
    }
    private val dayTextLineXPaint = Paint().apply {
        color = Color.BLACK
        textSize = 40f
        flags = Paint.ANTI_ALIAS_FLAG
        textAlign = Paint.Align.CENTER
    }
    private val xyTextPaint = Paint().apply {
        color = Color.BLACK
        textSize = 40f
        flags = Paint.ANTI_ALIAS_FLAG
        textAlign = Paint.Align.CENTER
    }

    private val rec = RectF()
    private var graphicsList: List<GraphicsData> = emptyList()
    private val padding = 40f

    private val widthLineX: Float get() = rec.right - (padding * 2)
    private val eachSectionWidth: Float get() = (widthLineX / 12)
    private val eachSectionCoordinateX: List<SectionCoordinateX> get() {
        var lastSectionX = padding
        val coordinates = ArrayList<SectionCoordinateX>()
        for (i in 0 until 12) {
            lastSectionX += eachSectionWidth
            coordinates.add(SectionCoordinateX(i + 1, lastSectionX))
        }
        return coordinates
    }
    class SectionCoordinateX(val day: Int, val coordinate: Float)

    private val heightLineY get() = rec.bottom - (padding * 2)
    private val eachSectionHeight get() = (heightLineY / 8)
    private val eachSectionCoordinateY: List<SectionCoordinateY> get() {
        var lastSectionY = rec.bottom - padding
        val coordinates = ArrayList<SectionCoordinateY>()
        for (i in 0 until 8) {
            lastSectionY -= eachSectionHeight
            coordinates.add(SectionCoordinateY((i + 1) * 1000, lastSectionY))
        }
        return coordinates
    }
    class SectionCoordinateY(val amount: Int, val coordinate: Float)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        var actualWidth = widthSize
        var actualHeight = heightSize

        when (widthMode) {
            MeasureSpec.EXACTLY,
            MeasureSpec.AT_MOST,
            MeasureSpec.UNSPECIFIED -> {
                if (actualWidth < actualHeight) {
                    actualHeight = actualWidth
                }
            }
        }

        when (heightMode) {
            MeasureSpec.EXACTLY,
            MeasureSpec.AT_MOST,
            MeasureSpec.UNSPECIFIED -> {
                if (actualHeight < actualWidth) {
                    actualWidth = actualHeight
                }
            }
        }

        val desiredSize = 500
        actualWidth = actualWidth.coerceAtLeast(desiredSize)
        actualHeight = actualHeight.coerceAtLeast(desiredSize)

        rec.right = actualWidth.toFloat()
        rec.bottom = actualHeight.toFloat()

        super.setMeasuredDimension(actualWidth, actualHeight)
    }

    override fun onDraw(canvas: Canvas) {
        drawVerticalY(canvas)
        drawHorizontalX(canvas)
        drawGraphicsLinesFromList(canvas)
    }

    private fun drawVerticalY(canvas: Canvas) {
        val startX = padding
        val startY = rec.bottom - padding
        val stopX = padding
        val stopY = padding
        canvas.drawLine(
            startX,
            startY,
            stopX,
            stopY,
            xyLinesPaint
        )
        canvas.drawText("Y", stopX, stopY - 5f, xyTextPaint)

        eachSectionCoordinateY.forEach {
            canvas.drawText(it.amount.toString(), stopX + 20f, it.coordinate + 20f, amountTextLineYPaint)
        }
    }

    private fun drawHorizontalX(canvas: Canvas) {
        val startX = padding
        val startY = rec.bottom - padding
        val stopX = rec.right - padding
        val stopY = rec.bottom - padding
        canvas.drawLine(
            startX,
            startY,
            stopX,
            stopY,
            xyLinesPaint
        )
        canvas.drawText("X", stopX + 20f, stopY + 15f, xyTextPaint)

        eachSectionCoordinateX.forEach {
            canvas.drawText(it.day.toString(), it.coordinate, stopY - 20f, dayTextLineXPaint)
        }
    }

    private fun drawGraphicsLinesFromList(canvas: Canvas) {
        val line = Path().apply {
            graphicsList.forEachIndexed { index, data ->
                data.x = eachSectionCoordinateX.find { it.day == data.id }?.coordinate ?: 0f
                eachSectionCoordinateY.find { it.amount > data.amount }?.let {
                    data.y = (data.amount.toFloat() / it.amount.toFloat()) * it.coordinate
                }
                if (index == 0) {
                    moveTo(data.x, data.y)
                } else {
                    val previous = graphicsList[index - 1]
                    val anchorX = previous.x + (data.x - previous.x) / 2f
                    cubicTo(
                        anchorX, previous.y,
                        anchorX, data.y,
                        data.x, data.y
                    )
                }
            }
        }
        canvas.drawPath(line, paint)
    }

    fun setPayloads(payloads: List<PayloadEntity>) {
        graphicsList = payloads.map { payload ->
            GraphicsData(
                id = payload.id,
                category = payload.category,
                amount = payload.amount,
                time = payload.time,
                x = 0f,
                y = 0f
            )
        }
        invalidate()
    }

    class GraphicsData(
        val id: Int,
        val category: String,
        val amount: Int,
        val time: Long,
        var x: Float,
        var y: Float
    )
}