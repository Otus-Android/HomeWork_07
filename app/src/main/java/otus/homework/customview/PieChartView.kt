package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.FontMetrics
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Region
import android.graphics.Typeface
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import otus.homework.customview.utils.dp

class PieChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private var onCategoryClickListener: ((category: String, amount: Int) -> Unit)? = null
    private var itemsData = mutableMapOf<String, Int>()
    private var totalAmount: Int = 0
    private var pathSections = mutableMapOf<String, Path>()

    private var centerX = 0f
    private var centerY = 0f
    private val chartRect = RectF()
    private var horizontalOffset = 0f
    private var verticalOffset = 0f

    override fun onDraw(canvas: Canvas) {
        var startAngle = -90f
        var sectionAngle = 0f
        val circleAngle = 360
        val paint = Paint()

        this.itemsData.forEach { (category, sum) ->
            sectionAngle = (sum.toFloat() / totalAmount) * circleAngle
            paint.apply {
                style = Paint.Style.FILL
                isAntiAlias = true
                color = context.getColorByCategory(category)
            }
            pathSections[category]?.arcTo(
                chartRect,
                startAngle,
                sectionAngle,
                true
            )
            pathSections[category]?.lineTo(centerX, centerY)
            pathSections[category]?.close()

            canvas.drawPath(pathSections[category]!!, paint)
            startAngle += sectionAngle
        }

        val totalAmountTextPaint: Paint = Paint().apply {
            textSize = 21.dp
            color = Color.BLACK
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val fm = FontMetrics()
        totalAmountTextPaint.getFontMetrics(fm)
        val widthText = totalAmountTextPaint.measureText(totalAmount.toString())

        val rectPaint = Paint().apply {
            style = Paint.Style.FILL
            color = Color.WHITE
            alpha = 0x80
        }
        val margin = 5.dp
        canvas.drawRect(
            width / 2f - widthText + margin,
            height / 2f + fm.top + margin,
            width / 2f + widthText,
            height / 2f + fm.bottom + margin * 2,
            rectPaint
        )

        canvas.drawText(
            "$totalAmount Pуб",
            width / 2f,
            height / 2f + totalAmountTextPaint.textSize / 3,
            totalAmountTextPaint
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val wMode = MeasureSpec.getMode(widthMeasureSpec)
        var wSize = MeasureSpec.getSize(widthMeasureSpec)
        val hMode = MeasureSpec.getMode(heightMeasureSpec)
        var hSize = MeasureSpec.getSize(heightMeasureSpec)

        when (wMode) {
            MeasureSpec.AT_MOST, MeasureSpec.UNSPECIFIED -> if (width > height) {
                wSize = height
            }
        }
        when (hMode) {
            MeasureSpec.AT_MOST, MeasureSpec.UNSPECIFIED -> if (height > width) {
                hSize = width
            }
        }

        horizontalOffset = if (hSize > wSize) 0f else (wSize - hSize).toFloat() / 2
        verticalOffset = if (wSize > hSize) 0f else (hSize - wSize).toFloat() / 2
        chartRect.left = horizontalOffset + paddingLeft
        chartRect.top = verticalOffset + paddingTop
        chartRect.right = wSize - horizontalOffset - paddingRight
        chartRect.bottom = hSize - verticalOffset - paddingBottom
        centerX = wSize.toFloat() / 2
        centerY = hSize.toFloat() / 2

        setMeasuredDimension(wSize, hSize)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            val rect = RectF()
            val region = Region()
            itemsData.forEach { (category, amount) ->
                pathSections[category]?.computeBounds(rect, false)
                region.setPath(
                    pathSections[category]!!,
                    Region(
                        rect.left.toInt(),
                        rect.top.toInt(),
                        rect.right.toInt(),
                        rect.bottom.toInt()
                    )
                )
                if (region.contains(event.x.toInt(), event.y.toInt())) {
                    onCategoryClickListener?.invoke(category, amount)
                }
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onSaveInstanceState(): Parcelable {
        return PieChartSavedState(super.onSaveInstanceState())
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state !is PieChartSavedState) {
            return super.onRestoreInstanceState(state)
        }
        super.onRestoreInstanceState(state.superState)
        setData(state.savedData)
    }

    fun setData(data: Map<String, Int>) {
        this.itemsData.putAll(data)
        totalAmount = data.map { it.value }.sum()
        pathSections = itemsData.mapValues { Path() } as MutableMap<String, Path>
        requestLayout()
        invalidate()
    }

    fun setOnCategoryClickListener(listener: (category: String, amount: Int) -> Unit) {
        this.onCategoryClickListener = listener
    }

    private fun Context.getColorByCategory(category: String): Int {
        return when (category) {
            "Продукты" -> getColor(R.color.purple_200)
            "Здоровье" -> getColor(R.color.purple_500)
            "Кафе и рестораны" -> getColor(R.color.purple_700)
            "Алкоголь" -> getColor(R.color.teal_200)
            "Доставка еды" -> getColor(R.color.teal_700)
            "Транспорт" -> getColor(R.color.blue)
            "Спорт" -> getColor(R.color.grey)
            else -> {
                getColor(R.color.black)
            }
        }
    }

    private inner class PieChartSavedState : BaseSavedState {
        val savedData = mutableMapOf<String, Int>()

        constructor(source: Parcelable?) : super(source) {
            savedData.putAll(itemsData)
        }

        private constructor(parcelIn: Parcel) : super(parcelIn) {
            parcelIn.readMap(savedData, ClassLoader.getSystemClassLoader())
        }

        override fun writeToParcel(parcelOut: Parcel, flags: Int) {
            super.writeToParcel(parcelOut, flags)
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
}