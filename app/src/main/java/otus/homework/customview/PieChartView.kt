package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import kotlin.math.roundToInt

class PieChartView(context: Context, attributeSet: AttributeSet)
    : View(context, attributeSet)
{
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

        when(widthMode) {
            MeasureSpec.EXACTLY, MeasureSpec.AT_MOST -> super.setMeasuredDimension(widthSize, heightSize)
            MeasureSpec.UNSPECIFIED -> super.setMeasuredDimension(
                defaultWidth - defaultMargin.toInt(),
                defaultHeight - defaultMargin.toInt()
            )
        }

        when(heightMode) {
            MeasureSpec.EXACTLY, MeasureSpec.AT_MOST -> super.setMeasuredDimension(widthSize, heightSize)
            MeasureSpec.UNSPECIFIED -> super.setMeasuredDimension(
                defaultWidth - defaultMargin.toInt(),
                defaultHeight - defaultMargin.toInt()
            )
        }
    }

    override fun onDraw(canvas: Canvas?) {
        if (canvas == null) return

        midWidth = width / 2f
        midHeight = height / 2f

//        canvas.drawArc(
//            midWidth - minOf(midWidth, midHeight) + defaultMargin,
//            midHeight - minOf(midWidth, midHeight) + defaultMargin,
//            midWidth + minOf(midWidth, midHeight) - defaultMargin,
//            midHeight + minOf(midWidth, midHeight) - defaultMargin,
//           0f,
//            90f,
//            false,
//            paint
//        )
//
//        canvas.drawArc(
//            midWidth - minOf(midWidth, midHeight) + defaultMargin,
//            midHeight - minOf(midWidth, midHeight) + defaultMargin,
//            midWidth + minOf(midWidth, midHeight) - defaultMargin,
//            midHeight + minOf(midWidth, midHeight) - defaultMargin,
//            91f,
//            90f,
//            false,
//            paint
//        )

        if (stores.isNotEmpty()){
            val left = midWidth - minOf(midWidth, midHeight) + defaultMargin
            val top = midHeight - minOf(midWidth, midHeight) + defaultMargin
            val right = midWidth + minOf(midWidth, midHeight) - defaultMargin
            val bottom = midHeight + minOf(midWidth, midHeight) - defaultMargin

            for (i in stores.indices){
                canvas.drawArc(
                    left,
                    top,
                    right,
                    bottom,
                    stores[i].startAngle,
                    stores[i].sweepAngle,
                    false,
                    pieChartPaints[i])
            }

            val textWidth = paintText.measureText("$TEXT_STORES ${stores.size}")

            val maxTextWidth = if (width < height) {
                (right - left - defaultMargin).toInt()
            } else {
                (bottom - top - defaultMargin).toInt()
            }

            if (textWidth > maxTextWidth) {
                canvas.drawText(
                    "$TEXT_STORES ${stores.size}",
                    midWidth,
                    midHeight + minOf(midWidth, midHeight) - defaultMargin + stroke,
                    paintText)
            } else {
                canvas.drawText("$TEXT_STORES ${stores.size}", midWidth, midHeight, paintText)
            }
        }
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

        for (i in stores.indices){
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
        const val TEXT_STORES= "Всего компаний: "
    }
}