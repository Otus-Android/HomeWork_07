package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import otus.homework.customview.data.PieChartModel
import otus.homework.customview.utils.dp
import otus.homework.customview.utils.px
import java.lang.Integer.min
import kotlin.math.atan2

class PieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var list: ArrayList<PieChartModel> = arrayListOf()
    private val paint = Paint().apply {
        style = Paint.Style.STROKE
    }
    private val rect = RectF()
    private var startAngel = -90f
    private var barWidth = 50.px.toFloat()
    private var angles =
        mutableListOf<Pair<String, Float>>()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val wMode = MeasureSpec.getMode(widthMeasureSpec)
        val hMode = MeasureSpec.getMode(heightMeasureSpec)
        val wSize = MeasureSpec.getSize(widthMeasureSpec)
        val hSize = MeasureSpec.getSize(heightMeasureSpec)

        when (wMode) {
            MeasureSpec.EXACTLY -> {
                setMeasuredDimension(wSize, hSize)
            }

            MeasureSpec.AT_MOST -> {
                val newW = min((list.size * barWidth).toInt(), wSize)
                setMeasuredDimension(newW, hSize)
            }

            MeasureSpec.UNSPECIFIED -> {
                setMeasuredDimension((list.size * barWidth).toInt(), hSize)
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val padding = if (measuredWidth.dp < 120) 30f else 100f

        angles.clear()

        rect.set(padding, padding, measuredWidth.minus(padding), measuredWidth.minus(padding))
        list.map { model ->
            canvas.drawArc(
                rect,
                startAngel,
                360f * model.percent,
                false,
                paint.apply {
                    color = model.color
                    strokeWidth = if (measuredWidth.dp < 120) 20f else 100f
                }
            )
            startAngel += 360f * model.percent
            angles.add(model.category to startAngel + 90)

        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val clickAngel = convertTouchEventPointToAngle(event.x, event.y).toFloat()
                for (i in angles.indices){
                    if (clickAngel < angles[i].second){
                        Toast.makeText(context, angles[i].first,Toast.LENGTH_LONG).show()
                        break
                    }
                }
            }
        }
        return true
    }

    private fun convertTouchEventPointToAngle(xPos: Float, yPos: Float): Double {
        val x = xPos - (measuredWidth * 0.5f)
        val y = yPos - (measuredWidth * 0.5f)

        var angle = Math.toDegrees(atan2(y.toDouble(), x.toDouble()) + Math.PI / 2)
        angle = if (angle < 0) angle + 360 else angle
        return angle
    }

    fun setList(newList: List<PieChartModel>) {
        list.clear()
        list.addAll(newList)
        requestLayout()
        invalidate()
    }
}
