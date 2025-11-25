package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.atan2

class PieChartView(context: Context, attr: AttributeSet) : View(context, attr) {

    private val paint = Paint().apply {
        style = Paint.Style.FILL
        flags = Paint.ANTI_ALIAS_FLAG
    }
    private val centerPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        flags = Paint.ANTI_ALIAS_FLAG
    }
    private val textPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL
        flags = Paint.ANTI_ALIAS_FLAG
    }

    private val colors = arrayOf(
        Color.CYAN,
        Color.RED,
        Color.YELLOW,
        Color.BLUE,
        Color.GREEN,
        Color.GRAY,
        Color.CYAN,
        Color.DKGRAY,
        Color.MAGENTA,
        Color.LTGRAY,
        Color.GREEN,
        Color.BLUE,
    )

    private var totalAmount = 0
    private val pieCartList = ArrayList<PieChartData>()

    private val rec = RectF()
    private val clickedPoint = PointF()

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
        actualWidth = if (rec.right > 0) rec.right.toInt() else actualWidth.coerceAtLeast(desiredSize)
        actualHeight = if (rec.bottom > 0) rec.bottom.toInt() else actualHeight.coerceAtLeast(desiredSize)

        rec.right = actualWidth.toFloat()
        rec.bottom = actualHeight.toFloat()

        super.setMeasuredDimension(actualWidth, actualHeight)
    }

    override fun onDraw(canvas: Canvas) {
        pieCartList.forEach {
            paint.color = it.color
            canvas.drawArc(rec, it.start, it.sweepAngle, true, paint)
        }

        val radius = rec.right / 2f - rec.right / 10
        val cx = rec.right / 2f
        val cy = rec.bottom / 2f
        canvas.drawCircle(cx, cy, radius, centerPaint)

        textPaint.textSize = 60f
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("$totalAmount", cx, cy, textPaint)
        textPaint.textSize = 40f
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("Total amount", cx, cy + 60, textPaint)

        if (clickedPoint.length() > 0) {
            textPaint.textSize = 30f
            textPaint.textAlign = Paint.Align.LEFT
            val angle = convertTouchEventPointToAngle(clickedPoint.x, clickedPoint.y)
            pieCartList.find { angle in it.start..it.end }?.let {
                canvas.drawText(
                    "Amount: ${it.amount}",
                    clickedPoint.x,
                    clickedPoint.y,
                    textPaint
                )
                canvas.drawText(
                    "Name: ${it.name}",
                    clickedPoint.x,
                    clickedPoint.y + 40,
                    textPaint
                )
                canvas.drawText(
                    "Category: ${it.category}",
                    clickedPoint.x,
                    clickedPoint.y + 80,
                    textPaint
                )
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            clickedPoint.set(event.x, event.y)
            invalidate()
        }
        return true
    }

    private fun convertTouchEventPointToAngle(xPos: Float, yPos: Float): Double {
        val x = xPos - (rec.right * 0.5f)
        val y = yPos - (rec.bottom * 0.5f)

        var angle = Math.toDegrees(atan2(y.toDouble(), x.toDouble()) + Math.PI / 2) - 90
        angle = if (angle < 0) angle + 360 else angle
        return angle
    }

    fun setPayloads(payloads: List<PayloadEntity>) {
        totalAmount = payloads.sumOf { it.amount }
        var start = 0f
        pieCartList.clear()
        payloads.forEachIndexed { index, payload ->
            val sweepAngle = (payload.amount.toFloat() / totalAmount.toFloat()) * 360f
            pieCartList.add(
                PieChartData(
                    start,
                    sweepAngle,
                    start + sweepAngle,
                    colors[index],
                    payload.name,
                    payload.category,
                    payload.amount
                )
            )
            start += sweepAngle
        }
        invalidate()
    }

    class PieChartData(
        val start: Float,
        val sweepAngle: Float,
        val end: Float,
        val color: Int,
        val name: String,
        val category: String,
        val amount: Int
    )

    override fun onSaveInstanceState(): Parcelable =
        SavedState(rec, clickedPoint, super.onSaveInstanceState())

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is SavedState) {
            super.onRestoreInstanceState(state.superState)
            rec.set(state.savedRec)
            clickedPoint.set(state.savedClickedPoint)
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    class SavedState(
        val savedRec: RectF,
        val savedClickedPoint: PointF,
        superState: Parcelable?
    ) : BaseSavedState(superState) {

        override fun writeToParcel(out: Parcel?, flags: Int) {
            super.writeToParcel(out, flags)
            out?.writeParcelable(savedRec, flags)
            out?.writeParcelable(savedClickedPoint, flags)
        }
    }
}