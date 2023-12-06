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
import otus.homework.customview.presentation.pie.chart.models.InnerPieAngleNode
import otus.homework.customview.presentation.pie.chart.models.PieData
import otus.homework.customview.presentation.pie.chart.models.PieDataProvider
import otus.homework.customview.presentation.pie.chart.models.PiePaints
import kotlin.math.atan2

class PieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paints = PiePaints()

    private val globalArea = Rect()
    private val localArea = RectF()
    private val chartArea = RectF()

    private var touchPointF = PointF()
    private var angle: Float = 0f

    private var style: PieStyle = PieStyle.PIE
    private var useCenter = false

    private val radius get() = localArea.width() / 2f
    private var currentValue: InnerPieAngleNode? = null

    fun setStyle(style: PieStyle) {
        this.style = style
        useCenter = style == PieStyle.DONUT
        if (style == PieStyle.PIE) {
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

    private val dataProvider = PieDataProvider()

    fun updateNodes(pieData: PieData<Float>) {
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
                if (localArea.contains(event.x, event.y)) {
                    // Toast.makeText(context, "${event.x} and ${event.y}", Toast.LENGTH_SHORT).show()
                }
                val radius = localArea.width() / 2f
                val xO = localArea.centerX() - event.x
                val yO = localArea.centerY() - event.y
                if (xO * xO + yO * yO < radius * radius) {
                    touchPointF.set(event.x, event.y)
                    invalidate()
                    // Toast.makeText(context, "${event.x} and ${event.y}", Toast.LENGTH_SHORT).show()

                    // val theta = Math.atan2()
                    var angleInDegrees = atan2(yO, xO)
                    if (angleInDegrees < 0) {
                        //  angleInDegrees = Math.PI. - angleInDegrees
                    }
                    val res = Math.toDegrees(angleInDegrees.toDouble())
                    // angle = res.toFloat()
                    angle = calculateThetaV2(event.x, event.y)

                    currentValue = dataProvider.getNode(angle)

                    Toast.makeText(context, dataProvider.getCategory(angle), Toast.LENGTH_SHORT)
                        .show()
                }
                true
            }

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
                localArea, node.angleStart, node.angleSweep, useCenter, paints.testchart
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
            canvas.drawArc(localArea, node.angleStart, node.angleSweep, true, Paint().apply {
                color = Color.GREEN
                style = Paint.Style.STROKE
                strokeWidth = 4f
            })

        }


    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    // right way
    private fun calculateTheta(x: Float, y: Float): Float {
        val xDistance = x - localArea.centerX().toDouble()
        val yDistance = y - localArea.centerY().toDouble()
        var angle = Math.toDegrees(atan2(-yDistance, xDistance))
        if (angle < 0f) angle += 360
        return angle.toFloat()
    }

    private fun calculateThetaV2(x: Float, y: Float): Float {
        val xDistance = x - localArea.centerX().toDouble()
        val yDistance = y - localArea.centerY().toDouble()
        var angle = Math.toDegrees(atan2(-yDistance, xDistance))
        if (angle < 0) {
            angle = -angle
        } else {
            angle = 360 - angle
        }
        return angle.toFloat()
    }
}


enum class PieStyle {
    PIE, DONUT
}