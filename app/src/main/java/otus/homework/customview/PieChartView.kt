package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import kotlin.math.roundToInt
import kotlin.random.Random

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
    private val strokes = arrayListOf(26f, 28f, 30f, 32f, 34f, 36f, 38f, 40f, 42f, 44f, 48f, 50f)
    private val pieChartPaints = arrayListOf<Paint>()

    private var stores: MutableList<Store> = mutableListOf()

    private val unspecifiedW = 256
    private val unspecifiedH = 256
    private val defaultMargin = (context.resources.displayMetrics.density * (strokes.minOrNull()?.div(2)  ?: 0f))
    private var defaultWidth = (context.resources.displayMetrics.density * unspecifiedW).toInt()
    private var defaultHeight = (context.resources.displayMetrics.density * unspecifiedH).toInt()

    private var midWidth = 0f
    private var midHeight = 0f
    private var startAngel = 0f
    private var sweepAngle = 0
    private val maxAngle = 360f

    override fun onSaveInstanceState(): Parcelable? {


        return super.onSaveInstanceState()
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        super.onRestoreInstanceState(state)
    }

//    private var defaultRadius: Float
//    init {
//        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.PieChartView)
//        defaultRadius = typedArray.getDimension(R.styleable.PieChartView_default_radius, 0f)
//        typedArray.recycle()
//    }

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

        if (stores.isNotEmpty()){
            for (i in stores.indices){
                canvas.drawArc(
                    midWidth - minOf(midWidth, midHeight) + defaultMargin,
                    midHeight - minOf(midWidth, midHeight) + defaultMargin,
                    midWidth + minOf(midWidth, midHeight) - defaultMargin,
                    midHeight + minOf(midWidth, midHeight) - defaultMargin,
                    stores[i].startAngle,
                    stores[i].sweepAngle,
                    false,
                    pieChartPaints[i])
            }
        }
    }

    fun setStores(newStores: List<Store>) {
        newStores.apply {
            val sumAmount = sumOf { it.amount }
            map { it.percentAmount = (it.amount.toFloat() / sumAmount) }
        }

        for (i in newStores.indices) {
            newStores[i].startAngle = startAngel + 1
            sweepAngle = (newStores[i].percentAmount * maxAngle).roundToInt()
            newStores[i].sweepAngle = sweepAngle.toFloat()
            startAngel += sweepAngle
        }

        stores.clear()
        stores.addAll(newStores)

        for (i in stores.indices){
            Paint().apply {
                color = resources.getColor(Colors.values()[i].rgb, null)
                strokeWidth = strokes[Random.nextInt(strokes.size)]
                style = Paint.Style.STROKE
                pieChartPaints.add(this)
            }
        }

        requestLayout()
        invalidate()
    }


}