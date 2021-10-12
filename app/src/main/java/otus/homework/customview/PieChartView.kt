package otus.homework.customview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

class PieChartView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {
    private enum class Colors(val rgb: Int) {
        GRAY(R.color.grey),
        CYAN(R.color.cyan),
        BLACK(R.color.black),
        RED(R.color.red),
        GREEN(R.color.green),
        MAGENTA(R.color.magenta),
        BLUE(R.color.blue),
        ORANGE(R.color.orange),
        TURQUOISE(R.color.turquoise),
        CUSTOM1(R.color.custom1),
        CUSTOM2(R.color.custom2),
        YELLOW(R.color.yellow)
    }

    private val stroke = 50f
    private val pieChartPaints = arrayListOf<Paint>()

    private val strokeRect = 20f
    private val paintRect = Paint().apply {
        color = resources.getColor(Colors.values()[0].rgb, null)
        strokeWidth = strokeRect
        style = Paint.Style.STROKE
    }

    private var stores: ArrayList<Store> = arrayListOf()

    private val unspecifiedW = 256
    private val unspecifiedH = 256
    private val defaultMargin = (context.resources.displayMetrics.density * stroke)
    private var defaultWidth = (context.resources.displayMetrics.density * unspecifiedW).toInt()
    private var defaultHeight = (context.resources.displayMetrics.density * unspecifiedH).toInt()

    private var midWidth = 0f
    private var midHeight = 0f
    private var startAngel = 0f
    private var sweepAngle = 0
    private val maxAngle = 360f
    private var outRadius = 0f
    private var inRadius = 0f

    private var lastXTouch: Float = 0.0f
    private var lastYTouch: Float = 0.0f

    private val paintText = Paint().apply {
        color = resources.getColor(R.color.black, null)
        style = Paint.Style.FILL
        textSize = 36f
        textAlign = Paint.Align.CENTER
        textSkewX = -0.2f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    //    private var defaultRadius: Float
//    init {
//        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.PieChartView)
//        defaultRadius = typedArray.getDimension(R.styleable.PieChartView_default_radius, 0f)
//        typedArray.recycle()
//    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        return PieChartViewState(superState, stores)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val pieChartViewState = state as? PieChartViewState
        super.onRestoreInstanceState(pieChartViewState?.superSavedState ?: state)

        stores = pieChartViewState?.stores ?: arrayListOf()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        when (widthMode) {
            MeasureSpec.EXACTLY, MeasureSpec.AT_MOST -> super.setMeasuredDimension(
                widthSize,
                heightSize
            )
            MeasureSpec.UNSPECIFIED -> super.setMeasuredDimension(
                defaultWidth - defaultMargin.toInt(),
                defaultHeight - defaultMargin.toInt()
            )
        }

        when (heightMode) {
            MeasureSpec.EXACTLY, MeasureSpec.AT_MOST -> super.setMeasuredDimension(
                widthSize,
                heightSize
            )
            MeasureSpec.UNSPECIFIED -> super.setMeasuredDimension(
                defaultWidth - defaultMargin.toInt(),
                defaultHeight - defaultMargin.toInt()
            )
        }
    }

    override fun onDraw(canvas: Canvas?) {
        canvas ?: return

        if (stores.isNotEmpty()) {
            midWidth = width / 2f
            midHeight = height / 2f
            val radius = minOf(midWidth, midHeight) - defaultMargin
            outRadius = radius + stroke / 2
            inRadius = radius - stroke / 2

            val left = midWidth - radius
            val top = midHeight - radius
            val right = midWidth + radius
            val bottom = midHeight + radius

//            canvas.drawRect(
//                left,
//                top,
//                right,
//                bottom,
//                paintRect
//            )
            drawStores(left, top, right, bottom, canvas)
            drawAllAmounts(right, left, bottom, top, canvas)
        }
    }

    private fun drawStores(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        canvas: Canvas,
    ) {
        for (i in stores.indices) {
            canvas.drawArc(
                left,
                top,
                right,
                bottom,
                stores[i].startAngle,
                stores[i].sweepAngle,
                false,
                pieChartPaints[i]
            )
        }
    }

    private fun drawAllAmounts(
        right: Float,
        left: Float,
        bottom: Float,
        top: Float,
        canvas: Canvas
    ) {
        val textWidth = paintText.measureText("$STORES_ALL_AMOUNT ${stores.size}")
        val maxTextWidth =
            if (width < height) (right - left - defaultMargin).toInt() else (bottom - top - defaultMargin).toInt()

        if (textWidth > maxTextWidth) {
            canvas.drawText(
                "$STORES_ALL_AMOUNT ${stores.size}",
                midWidth,
                midHeight + minOf(midWidth, midHeight) - defaultMargin + stroke,
                paintText
            )
        } else {
            canvas.drawText("$STORES_ALL_AMOUNT ${stores.size}", midWidth, midHeight, paintText)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastXTouch = event.x
                lastYTouch = event.y
            }
            MotionEvent.ACTION_UP -> {
                val distanceBetweenPoints = sqrt((lastXTouch - midWidth).pow(2) +
                    (lastYTouch - midHeight).pow(2))

                if (distanceBetweenPoints in inRadius..outRadius) {
                    val angle = Math.toDegrees( atan2(lastYTouch.toDouble() - midHeight, lastXTouch.toDouble() - midWidth) )
                    val angleAbs = if (angle >= 0.0) angle else angle + maxAngle

                    val store = stores.filter {
                        angleAbs in it.startAngle..it.startAngle + it.sweepAngle
                    }

                    Toast.makeText(
                        this.context,
                        """$SECTOR
                    | $NAME ${store[0].name} 
                    | $CATEGORY ${store[0].category}
                    | $AMOUNT: ${store[0].amount}""".trimMargin(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        return true
    }

    fun setStores(newStores: List<Store>) {
        newStores.apply {
            val sumAmount = sumOf { it.amount }
            map { it.percentAmount = (it.amount.toFloat() / sumAmount) }
        }

        for (i in newStores.indices) {
            newStores[i].startAngle = startAngel
            sweepAngle = (newStores[i].percentAmount * maxAngle).roundToInt()
            newStores[i].sweepAngle = sweepAngle.toFloat() - 1
            startAngel += sweepAngle
        }

        stores.clear()
        stores.addAll(newStores)

        for (i in stores.indices) {
            Paint().apply {
                color = resources.getColor(Colors.values()[i].rgb, null)
                strokeWidth = stroke
                style = Paint.Style.STROKE
                pieChartPaints.add(this)
            }
        }

        requestLayout()
        invalidate()
    }

    companion object {
        private const val STORES_ALL_AMOUNT = "Всего компаний: "
        private const val SECTOR = "Нажали на сектор"
        private const val NAME = "Наименование:"
        private const val CATEGORY = "Категория:"
        private const val AMOUNT = "Кол-во:"
    }
}