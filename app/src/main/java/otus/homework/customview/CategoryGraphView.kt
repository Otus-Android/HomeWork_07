package otus.homework.customview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class CategoryGraphView : View {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val state = State()

    private val padding = 15f
    private val defSize = 500
    private val greyLineStep = 100 // В рублях

    private val firstDate = 1622494800
    private val lastDate = 1625086800

    private val gPaint: Paint
    private val axisPaint: Paint
    private val linePaint: Paint
    private val textPaint: Paint
    private val orderService = OrderService()

    private var actSize = defSize

    private var category: String? = null

    init {
        gPaint = Paint().apply {
            color = Color.RED
            strokeWidth = 5f
            isAntiAlias = true
        }
        axisPaint = Paint().apply {
            color = Color.BLACK
            strokeWidth = 2f
            isAntiAlias = true
        }
        linePaint = Paint().apply {
            color = Color.LTGRAY
        }
        textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 20f
            isAntiAlias = true
        }
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

        category?.let {
            drawAxes(canvas, it)

            var lastX = padding
            var lastY = actSize - padding
            if (state.gPath.isEmpty()) {
                val max = orderService.getOrdersForGraph(it).first
                for (date in firstDate until lastDate step 86400) {
                    var sum = 0f
                    for (order in orderService.getOrdersForGraph(it).second) {
                        if (order.time.toLong() > date && order.time.toLong() <= date + 86400) {
                            sum += order.amount.toFloat()
                        }
                    }
                    val stopY = (actSize - padding) * (1 - (sum / max.toFloat()))
                    canvas.drawLine(lastX, lastY, lastX + 20, stopY, gPaint)
                    lastX += 20
                    lastY = stopY

                    state.addToPath(lastX, lastY)
                }
            } else {
                for (point in state.gPath) {
                    canvas.drawLine(lastX, lastY, point.x, point.y, gPaint)
                    lastX = point.x
                    lastY = point.y
                }
            }

            drawTxts(canvas)
        }
    }

    fun setCategory(value: String) {
        category = value
        state.clear()
        invalidate()
    }

    private fun drawAxes(canvas: Canvas, category: String) {
        // X
        canvas.drawLine(padding, actSize - padding, actSize - padding, actSize - padding, axisPaint)
        // Y
        canvas.drawLine(padding, actSize - padding, padding, padding, axisPaint)
        // Grey lines
        if (state.linesY.isEmpty()) {
            val max = orderService.getOrdersForGraph(category).first
            if (max > greyLineStep) {
                for (y in greyLineStep until max step greyLineStep) {
                    val lineY = (actSize - padding) * (1 - (y.toFloat() / max.toFloat()))
                    canvas.drawLine(padding, lineY, actSize - padding, lineY, linePaint)

                    state.addToLinesY(lineY)
                }
            }
        } else {
            for (lineY in state.linesY) {
                canvas.drawLine(padding, lineY, actSize - padding, lineY, gPaint)
            }
        }
    }

    private fun drawTxts(canvas: Canvas) {
        textPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("Траты", padding, padding, textPaint)
        textPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Дата", actSize - padding, actSize - padding, textPaint)
    }

    class State {
        val gPath: MutableList<PointF> = mutableListOf()
        val linesY: MutableList<Float> = mutableListOf()

        fun addToPath(x: Float, y: Float) {
            gPath.add(PointF(x, y))
        }

        fun addToLinesY(y: Float) {
            linesY.add(y)
        }

        fun clear() {
            gPath.clear()
            linesY.clear()
        }
    }
}