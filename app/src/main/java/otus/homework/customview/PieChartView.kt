package otus.homework.customview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import otus.homework.customview.state.PieChartState
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class PieChartView : View {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private val state = PieChartState()

    private val piePaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    private val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = 30f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    private val orderService = Service()
    private var selectedId: String? = null
    private val sectorsColors: IntArray = context.resources.getIntArray(R.array.sectorsColors)

    private val defaultSize = 500
    private var actSize = defaultSize
    private val padding = 15f

    var onOrderClick: ((String) -> (Unit))? = null

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)

        var size = minOf(heightSize, widthSize)

        if (size < defaultSize) size = defaultSize

        actSize = if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(defaultSize, defaultSize)
            defaultSize
        } else {
            setMeasuredDimension(size, size)
            size
        }
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        var angle = 0f
        if (state.sectors.isEmpty()) {
            var colorIterator = sectorsColors.iterator()

            for (order in orderService.getOrdersForPie()) {
                //если, итератор больше не имеет цветов, то запускаем по второму кругу
                if (!colorIterator.hasNext()) colorIterator = sectorsColors.iterator()
                piePaint.color = colorIterator.next()
                //если ордер уже выбран, то падинги меньше, соответственно сектор больше
                val p = if (order.key.id == selectedId) 0f else padding
                canvas.drawArc(p, p, actSize - p, actSize - p, angle, order.value, true, piePaint)

                canvas.drawText(
                    order.key.name,
                    getXByAngle(angle + order.value / 2) + actSize / 2f,
                    getYByAngle(angle + order.value / 2) + actSize / 2f,
                    textPaint
                )

                angle += order.value

                state.addSector(
                    order.value,
                    order.key.id,
                    order.key.name,
                    order.key.category,
                    piePaint.color
                )
            }
        } else {
            for (sector in state.sectors) {
                piePaint.color = sector.color
                val p = if (sector.id == selectedId) 0f else padding
                canvas.drawArc(p, p, actSize - p, actSize - p, angle, sector.angle, true, piePaint)
                canvas.drawText(
                    sector.name, getXByAngle(angle + sector.angle / 2) + actSize / 2f,
                    getYByAngle(angle + sector.angle / 2) + actSize / 2f,
                    textPaint
                )
                angle += sector.angle
            }
        }
    }

    private fun getXByAngle(angle: Float): Float {
        return cos(Math.toRadians(angle.toDouble())).toFloat() * actSize * .45f
    }

    private fun getYByAngle(angle: Float): Float {
        return sin(Math.toRadians(angle.toDouble())).toFloat() * actSize * .45f
    }

    private fun getAngleByXY(x: Float, y: Float): Float {
        val angle = Math.toDegrees(atan2(y, x).toDouble()).toFloat()
        return if (angle < 0) angle + 360 else angle
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            //берем координату клика и вычитаем из него координату центральной точки круга и получаем расстояние до центра.
            val centeredX = event.x - actSize / 2f
            val centeredY = event.y - actSize / 2f
            val radius = sqrt(centeredX * centeredX.toDouble() + centeredY * centeredY.toDouble())

            //если, от точки нажатия до центра расстояния меньше чем половина actSize
            if (radius < actSize * .5) {
                //получаем градус, где было зафиксировано нажатие относительно точки 0
                val touchAngle = getAngleByXY(centeredX, centeredY)
                var angle = 0f

                if (state.sectors.isEmpty()) {
                    for (order in orderService.getOrdersForPie()) {

                        if (touchAngle > angle && touchAngle < angle + order.value) {
                            if (selectedId != order.key.id) {
                                selectedId = order.key.id
                                invalidate()
                                onOrderClick?.invoke(order.key.category)
                            }
                            return true
                        }
                        angle += order.value
                    }
                } else {
                    for (sector in state.sectors) {
                        //находим тот который соответствует нашему тапу
                        if (touchAngle > angle && touchAngle < angle + sector.angle) {
                            if (sector.id != selectedId) {
                                selectedId = sector.id
                                invalidate()
                                requestLayout()
                                onOrderClick?.invoke(sector.category)
                            }
                            return true
                        }
                        angle += sector.angle
                    }
                }
            }
        }
        return false
    }


    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        bundle.putSerializable("state", state)
        bundle.putParcelable("superState", super.onSaveInstanceState())
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        var viewState = state
        if (viewState is Bundle) {
            val restoreState = viewState.getSerializable("state") as PieChartState
            Log.i("11111", "onRestoreInstanceState: ${restoreState.sectors}")

            viewState = viewState.getParcelable("superState")

        }
        super.onRestoreInstanceState(viewState)
    }
}