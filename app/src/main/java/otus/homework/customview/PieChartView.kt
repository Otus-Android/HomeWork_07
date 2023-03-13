package otus.homework.customview

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import java.util.Random

private const val TOTAL_DEGREES = 360f
class PieChartView : View {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?
    ) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr)

    private val totalAmount = TotalAmount()
    private val categoriesToDraw = mutableMapOf<String, CategoryVisualisationModel>()
    private val data = mutableMapOf<String, Int>()

    val Int.dp: Float
        get() = this * Resources.getSystem().displayMetrics.density

    val Float.dp: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val minContentWidth = 260.dp.toInt()
        val minContentHeight = 260.dp.toInt()

        when {
            widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST -> {
                setMeasuredDimension(
                    minContentWidth,
                    minContentHeight
                )
            }
            widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.AT_MOST -> {
                setMeasuredDimension(
                    widthSize.coerceAtLeast(minContentWidth),
                    minContentHeight
                )
            }
            widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.EXACTLY -> {
                setMeasuredDimension(
                    minContentWidth,
                    heightSize.coerceAtLeast(minContentHeight)
                )
            }
            widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY -> {
                setMeasuredDimension(
                    widthSize.coerceAtLeast(minContentWidth),
                    heightSize.coerceAtLeast(minContentHeight)
                )
            }
            else -> {
                // nothing to do
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(Color.LTGRAY)
        this.categoriesToDraw.forEach {
            canvas.drawPath(it.value.path, it.value.paint)
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        return PieChartSavedState(super.onSaveInstanceState())
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state !is PieChartSavedState) {
            return super.onRestoreInstanceState(state)
        }
        super.onRestoreInstanceState(state.superState)
        data.putAll(state.savedData)
        totalAmount.update(
            amount = data.map { it.value }.sum()
        )
        categoriesToDraw.populate(data)
        invalidate()
    }

    fun updateData(data: Map<String, Int>) {
        this.data.putAll(data)
        totalAmount.update(
            amount = data.map { it.value }.sum()
        )
        categoriesToDraw.populate(data)
        invalidate()
    }

    private fun MutableMap<String, CategoryVisualisationModel>.populate(
        data: Map<String, Int>
    ) {
        clear()
        var startAngle = 0f
        data.forEach {
            val category = CategoryVisualisationModel(
                categoryAmount = it.value,
                totalAmount = totalAmount.getTotalAmount(),
                startAngle = startAngle
            )
            startAngle = category.endAngle
            put(it.key, category)
            "${startAngle}".log()
        }
    }

    private inner class CategoryVisualisationModel(
        categoryAmount: Int,
        totalAmount: Int,
        startAngle: Float
    ) {
        val path: Path = Path()
        val paint: Paint = Paint()
        val endAngle: Float
        init {
            val rectF = RectF(60f, 60f, 240.dp, 240.dp)
            val sweepAngel = TOTAL_DEGREES * categoryAmount / totalAmount
            path.apply {
                addArc(rectF, startAngle, sweepAngel)
            }
            paint.apply {
                this.color = generateRandomColor()
                this.style = Paint.Style.STROKE
                this.strokeWidth = 120f
            }
            endAngle = startAngle + sweepAngel
        }

        private fun generateRandomColor(): Int {
            val random = Random()
            return Color.argb(
                255,
                random.nextInt(256),
                random.nextInt(256),
                random.nextInt(256)
            )
        }
    }

    private inner class PieChartSavedState : BaseSavedState {

        val savedData = mutableMapOf<String, Int>()

        constructor(source: Parcelable?) : super(source) {
            savedData.putAll(data)
        }
        private constructor(parcelIn: Parcel) : super(parcelIn) {
            parcelIn.readMap(savedData, ClassLoader.getSystemClassLoader())
        }
        override fun writeToParcel(parcelOut: Parcel, flags: Int) {
            super.writeToParcel(parcelOut, flags)
            savedData.isEmpty().toString().log()
            parcelOut.writeMap(savedData)
        }

        @JvmField
        val CREATOR: Parcelable.Creator<PieChartSavedState?> =
            object : Parcelable.Creator<PieChartSavedState?> {
            override fun createFromParcel(parcelIn: Parcel): PieChartSavedState {
                return PieChartSavedState(parcelIn)
            }

            override fun newArray(size: Int): Array<PieChartSavedState?> {
                return arrayOfNulls(size)
            }
        }
    }

    private data class TotalAmount(
        private var totalAmount: Int = 0
    ) {
        fun getTotalAmountFormatted(): String {
            return if (totalAmount == 0) "No expenses"
            else "You spent\n$totalAmount $"
        }

        fun getTotalAmount(): Int = totalAmount

        fun update(amount: Int) {
            if (this.totalAmount != amount)
                this.totalAmount = amount
        }
    }
}