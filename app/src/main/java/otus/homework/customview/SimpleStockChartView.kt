package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.CornerPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class SimpleStockChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val items = ArrayList<ChartItem>()

    private  val path: Path = Path()

    private val axisPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    private val linePaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 4f
        pathEffect = CornerPathEffect(30f)
    }
    private val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = 32f
        textAlign = Paint.Align.CENTER
    }

    private val formatterD = DateTimeFormatter.ofPattern("dd")
    private val formatterM = DateTimeFormatter.ofPattern("MMM")

    fun setData(data: List<ChartItem>) {
        items.clear()
        if (data.isEmpty()) return
        items.addAll(data)
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        //???
        val wMode = MeasureSpec.getMode(widthMeasureSpec)
        val wSize = MeasureSpec.getSize(widthMeasureSpec)
        val hSize = MeasureSpec.getSize(heightMeasureSpec)

        when (wMode) {
            MeasureSpec.EXACTLY,
            MeasureSpec.AT_MOST,
            MeasureSpec.UNSPECIFIED -> setMeasuredDimension(wSize, hSize)
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (items.isEmpty()) return
        canvas?.let { c ->
            drawAxes(c)
            drawLineGraph(c)
        }
    }

    private fun drawAxes(canvas: Canvas) {
        canvas.drawLine(
            paddingLeft.toFloat(),
            height - paddingBottom.toFloat(),
            width - paddingRight.toFloat(),
            height - paddingBottom.toFloat(),
            axisPaint
        )
        canvas.drawLine(
            paddingLeft.toFloat(),
            height - paddingBottom.toFloat(),
            paddingLeft.toFloat(),
            paddingTop.toFloat(),
            axisPaint
        )

        val stepX = stepX()
        for (i in items.indices) {
            val x = paddingLeft + i * stepX
            canvas.drawText(
                items[i].time.format(formatterD),
                x,
                height - paddingBottom + textPaint.textSize,
                textPaint
            )
            canvas.drawText(
                items[i].time.format(formatterM),
                x,
                height - paddingBottom + 2 * textPaint.textSize,
                textPaint
            )
        }

        val maxY = items.maxOf { it.amount }
        var minY = items.minOf { it.amount }
        if (maxY == minY) minY = 0.0
        val rangeY = maxY - minY
        val stepY = (height - paddingTop - paddingBottom).toFloat() / rangeY
        for (yValue in minY.toInt()..maxY.toInt() step (rangeY / 5).toInt()) {
            val y = height - paddingBottom - (yValue - minY) * stepY
            canvas.drawText(
                yValue.toString(),
                paddingLeft - textPaint.measureText(yValue.toString()),
                y.toFloat(),
                textPaint
            )
        }
    }

    private fun drawLineGraph(canvas: Canvas) {
        path.reset()
        val stepX = stepX()

        val maxY = items.maxOf { it.amount }
        var minY = items.minOf { it.amount }
        if (maxY == minY) minY = 0.0
        val rangeY = maxY - minY
        val stepY = (height - paddingTop - paddingBottom).toFloat() / rangeY

        var currentX = paddingLeft.toFloat()
        for ((index, value) in items.withIndex()) {
            val y = height - paddingBottom - (value.amount - minY) * stepY
            if (index == 0) {
                path.moveTo(currentX, y.toFloat())
                if (items.size == 1) path.lineTo(currentX, (height - paddingBottom).toFloat())
            } else {
                path.lineTo(currentX, y.toFloat())
            }
            currentX += stepX
        }

        canvas.drawPath(path, linePaint)
    }

    private fun stepX(): Float {
        val step = if (items.size == 1) 1 else items.size - 1
        val stepX = (width - paddingLeft - paddingRight).toFloat() / step
        return stepX
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val savedState = SavedState(superState).apply {
            dataArray = items.toTypedArray()
        }
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }
        super.onRestoreInstanceState(state.superState)
        items.addAll(state.dataArray)
        requestLayout()
    }

    data class ChartItem(
        val time: LocalDate,
        val amount: Double
    ) :
        Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readSerializable() as LocalDate,
            parcel.readDouble()
        )

        override fun writeToParcel(dest: Parcel?, flags: Int) {
            dest?.writeSerializable(time)
            dest?.writeDouble(amount)
        }

        override fun describeContents(): Int = 0

        companion object CREATOR : Parcelable.Creator<ChartItem> {
            override fun createFromParcel(parcel: Parcel): ChartItem {
                return ChartItem(parcel)
            }

            override fun newArray(size: Int): Array<ChartItem?> {
                return arrayOfNulls(size)
            }
        }
    }

    internal class SavedState : BaseSavedState {
        lateinit var dataArray: Array<ChartItem>

        constructor(superState: Parcelable?) : super(superState)

        constructor(source: Parcel) : super(source) {
            dataArray = source.createTypedArray(ChartItem.CREATOR) ?: emptyArray()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeTypedArray(dataArray, flags)
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState {
                return SavedState(parcel)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }
}