package otus.homework.customview.presentation.pie.chart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import otus.homework.customview.presentation.pie.chart.models.PieAreaNode
import otus.homework.customview.presentation.pie.chart.models.PieAreaProvider
import otus.homework.customview.presentation.pie.chart.models.PiePaints
import otus.homework.customview.presentation.pie.chart.utils.MathUtils

class PieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paints = PiePaints()

    /* Вся область view */
    private val globalArea = Rect()
    private val localArea = RectF()
    private val chartArea = RectF()

    private var touchPointF = PointF()
    private var angle: Float = 0f

    private var style: PieStyle = PieStyle.PIE
    private var useCenter = false

    private var currentValue: PieAreaNode? = null

    private val dataProvider = PieAreaProvider()

    fun setStyle(style: PieStyle) {
        this.style = style
        useCenter = style == PieStyle.PIE
        if (style == PieStyle.DONUT) {
            paints.testchart.apply {
                this.style = Paint.Style.STROKE
                strokeWidth = 40f
            }
        } else {
            paints.testchart.apply {
                this.style = Paint.Style.FILL
                strokeWidth = 0f
            }
        }
        invalidate()
    }

    fun render(pieData: PieData) {
        dataProvider.calculate(pieData)
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        globalArea.set(0, 0, w, h)

        val localAreaRadius = minOf(w, h) / 2f
        val centrePoint = PointF(w / 2f, h / 2f)

        localArea.set(/* left = */ centrePoint.x - localAreaRadius + paints.testchart.strokeWidth,/* top = */
            centrePoint.y - localAreaRadius + paints.testchart.strokeWidth,/* right = */
            centrePoint.x + localAreaRadius - paints.testchart.strokeWidth,/* bottom = */
            centrePoint.y + localAreaRadius - paints.testchart.strokeWidth
        )

        chartArea.set(/* left = */ centrePoint.x - localAreaRadius + paints.testchart.strokeWidth / 2f,/* top = */
            centrePoint.y - localAreaRadius + paints.testchart.strokeWidth / 2f,/* right = */
            centrePoint.x + localAreaRadius - paints.testchart.strokeWidth / 2f,/* bottom = */
            centrePoint.y + localAreaRadius - paints.testchart.strokeWidth / 2f
        )
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val radius = localArea.width() / 2f
                val xO = localArea.centerX() - event.x
                val yO = localArea.centerY() - event.y
                if (xO * xO + yO * yO < radius * radius) {
                    touchPointF.set(event.x, event.y)
                    invalidate()
                    // Toast.makeText(context, "${event.x} and ${event.y}", Toast.LENGTH_SHORT).show()

                    angle = MathUtils.calculateThetaV2(
                        localArea.centerX(),
                        localArea.centerY(),
                        event.x,
                        event.y
                    )

                    currentValue = dataProvider.getNode(angle)

                    Toast.makeText(context, dataProvider.getCategory(angle), Toast.LENGTH_SHORT)
                        .show()
                }
                true
            }

            /*         MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                         touchPointF.set(-1f, -1f)
                         invalidate()
                         true
                     }*/

            else -> false
        }
    }

    override fun onDraw(canvas: Canvas) {

        canvas.drawColor(Color.YELLOW)
        canvas.drawRect(globalArea, paints.global)
        canvas.drawRect(localArea, paints.local)
        canvas.drawRect(chartArea, paints.chart)

        dataProvider.getNodes().forEach { node ->
            paints.testchart.color = node.color
            canvas.drawArc(
                localArea, node.startAngle, node.sweepAngle, useCenter, paints.testchart
            )
        }

        canvas.drawArc(localArea, 0f, 10f, true, Paint().apply { color = Color.BLACK })

        if (!touchPointF.equals(0f, 0f)) {
            canvas.drawLine(localArea.centerX(),
                localArea.centerY(),
                touchPointF.x,
                touchPointF.y,
                Paint().apply {
                    color = Color.BLUE
                    strokeWidth = 10f
                })
        }

        canvas.drawText(angle.toString(), localArea.centerX(), localArea.bottom, Paint().apply {
            color = Color.BLACK
            textSize = 64f
        })

        currentValue?.let { node ->
            canvas.drawArc(localArea, node.startAngle, node.sweepAngle, true, Paint().apply {
                color = Color.GREEN
                style = Paint.Style.STROKE
                strokeWidth = 4f
            })

        }
    }
}