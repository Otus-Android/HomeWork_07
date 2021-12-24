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
import otus.homework.customview.model.Colors
import otus.homework.customview.model.Store
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

class PieChartView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {
    private val stroke = convertDpToPixels(STROKE)
    private val pieChartPaints = arrayListOf<Paint>()

    private var stores: ArrayList<Store> = arrayListOf()

    private val defaultMargin = stroke
    private var defaultWidth = convertDpToPixels(UNSPECIFIED_W).toInt()
    private var defaultHeight = convertDpToPixels(UNSPECIFIED_H).toInt()

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
        textSize = convertSpToPixels(TEXT_SIZE)
        textAlign = Paint.Align.CENTER
        textSkewX = TEXT_SKEW_X
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

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

            drawStores(midWidth - radius, midHeight - radius, midWidth + radius, midHeight + radius, canvas)
            drawAllAmounts(midWidth - radius, midHeight - radius, midWidth + radius, midHeight + radius, canvas)
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
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
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

        private const val STROKE = 10f
        private const val UNSPECIFIED_W = 256f
        private const val UNSPECIFIED_H = 256f
        private const val TEXT_SIZE = 16f
        private const val TEXT_SKEW_X = -0.2f
    }
}