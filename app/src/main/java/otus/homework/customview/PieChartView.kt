package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class PieChartView : View {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val state = State()

    private val padding = 15f
    private val defSize = 500

    private val piePaint : Paint
    private val textPaint : Paint
    private val orderService = OrderService()
    private val colors : IntArray

    private var selectedId : String? = null
    private var actSize = defSize

    var onOrderClick: ((String) -> (Unit))? = null

    init {
        piePaint = Paint().apply {
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 20f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        colors = context.resources.getIntArray(R.array.sliceColors)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        var size = minOf(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec))
        if (size < defSize) size = defSize

        actSize = if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(defSize, defSize)
            defSize
        } else {
            setMeasuredDimension(size, size)
            size
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (canvas == null) return

        var angle = 0f
        if (state.slices.isEmpty()) {
            var colorIterator = colors.iterator()
            for (order in orderService.getOrdersForPie()) {
                if (!colorIterator.hasNext()) colorIterator = colors.iterator()
                piePaint.color = colorIterator.next()
                val p = if (order.key.id == selectedId) 0f else padding
                canvas.drawArc(p, p, actSize - p, actSize - p, angle, order.value, true, piePaint)
                if (order.value > 15) {
                    canvas.drawText(
                        order.key.name,
                        getXByAngle(angle + order.value / 2) + actSize / 2f,
                        getYByAngle(angle + order.value / 2) + actSize / 2f,
                        textPaint
                    )
                }
                angle += order.value

                state.addSlice(
                    order.value,
                    order.key.id,
                    order.key.name,
                    order.key.category,
                    piePaint.color
                )
            }
        } else {
            for (slice in state.slices) {
                piePaint.color = slice.color
                val p = if (slice.id == selectedId) 0f else padding
                canvas.drawArc(p, p, actSize - p, actSize - p, angle, slice.angle, true, piePaint)
                if (slice.angle > 15) {
                    canvas.drawText(
                        slice.name,
                        getXByAngle(angle + slice.angle / 2) + actSize / 2f,
                        getYByAngle(angle + slice.angle / 2) + actSize / 2f,
                        textPaint
                    )
                }
                angle += slice.angle
            }
        }

        piePaint.color = Color.WHITE
        canvas.drawCircle(actSize / 2f, actSize / 2f, actSize*.3f, piePaint)
    }

    private fun getXByAngle(angle:Float): Float {
        return cos(Math.toRadians(angle.toDouble())).toFloat() * actSize*.45f
    }

    private fun getYByAngle(angle:Float): Float {
        return sin(Math.toRadians(angle.toDouble())).toFloat() * actSize*.45f
    }

    private fun getAngleByXY(x:Float, y:Float): Float {
        val angle = Math.toDegrees(atan2(y, x).toDouble()).toFloat()
        return if (angle < 0) angle + 360 else angle
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            // Центрируем координаты к центру круга
            val centeredX = event.x - actSize / 2f
            val centeredY = event.y - actSize / 2f
            // Находим расстояние от центра до тапа
            val radius = sqrt(centeredX*centeredX.toDouble() + centeredY*centeredY.toDouble())
            // Тап должен попадать в полосу диаграммы
            if (radius < actSize*.5 && radius > actSize*.3) {
                // Если тап попал по полосе, то ищем нужную секцию
                val touchAngle = getAngleByXY(centeredX, centeredY)
                var angle = 0f
                if (state.slices.isEmpty()) {
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
                    for (slice in state.slices) {
                        if (touchAngle > angle && touchAngle < angle + slice.angle) {
                            if (slice.id != selectedId) {
                                selectedId = slice.id
                                invalidate()
                                onOrderClick?.invoke(slice.category)
                            }
                            return true
                        }
                        angle += slice.angle
                    }
                }
            }
        }
        return false
    }

    data class Slice(
        var angle: Float,
        var id: String,
        var name: String,
        var category: String,
        var color: Int
    )

    class State {
        val slices: MutableList<Slice> = mutableListOf()

        fun addSlice(angle: Float, id: String, name: String, category: String, color: Int) {
            slices.add(Slice(angle, id, name, category, color))
        }

        fun clear() {
            slices.clear()
        }
    }
}