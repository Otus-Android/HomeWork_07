package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.Integer.min
import kotlin.math.atan2
import kotlin.math.max

class PieChart @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): View(context, attrs) {

    var onCategoryClickListener: OnCategoryClickListener? = null

    private val colors = arrayListOf(
        Color.RED,
        Color.GREEN,
        Color.GRAY,
        Color.CYAN,
        Color.MAGENTA,
        Color.YELLOW,
        Color.BLUE,
        Color.DKGRAY,
        Color.BLACK,
        Color.LTGRAY,
        Color.BLUE
    )

    private val defaultRadius = 50.dp
    private var radius = defaultRadius
    private val strokeWidth = 50.dp.toFloat()
    private val maxCategories = 10 // Сколько категорий отображать. Остальные сгруппируются

    private var data: List<Category.OneCategory> = listOf()
    private var chartData: List<Category> = listOf()
    private var allSum = 0

    private var circleX = 0
    private var circleY = 0

    private val noDataPaint: Paint = Paint().apply {
        textSize = 72.sp.toFloat()
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
    }
    private val defaultHeight = 100.dp

    private val categoryPaint: Paint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = this@PieChart.strokeWidth
    }

    private val chartRect = RectF()

    init {

        if (isInEditMode) {
            setup(listOf(
                Category.OneCategory("Продукты", 3500),
                Category.OneCategory("Транспорт", 1000),
                Category.OneCategory("Спорт", 1648),
                Category.OneCategory("Кафе и рестораны", 800),
                Category.OneCategory("Доставка еды", 364),
                Category.OneCategory("Здоровье", 981),
                Category.OneCategory("Образование", 2500),
                Category.OneCategory("Развлечения", 884),
                Category.OneCategory("Благотворительность", 1200),
                Category.OneCategory("Разное", 3000)
            ))
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val wMode = MeasureSpec.getMode(widthMeasureSpec)
        val hMode = MeasureSpec.getMode(heightMeasureSpec)
        val wSize = MeasureSpec.getSize(widthMeasureSpec)
        val hSize = MeasureSpec.getSize(heightMeasureSpec)

        if (chartData.isEmpty()) {
            setMeasuredDimension(wSize, max(defaultHeight, hSize))
            return
        }

        val newW: Int = when (wMode) {
            MeasureSpec.EXACTLY -> {
                wSize
            }
            MeasureSpec.AT_MOST -> {
                min(hSize, wSize)
            }
            else -> {
                if (hMode == MeasureSpec.UNSPECIFIED)
                    (defaultRadius + strokeWidth * 2).toInt()
                else
                    hSize
            }
        }

        when (hMode) {
            MeasureSpec.EXACTLY -> {
                setMeasuredDimension(newW, hSize)
            }
            MeasureSpec.AT_MOST -> {
                if (newW < hSize) {
                    setMeasuredDimension(newW, newW)
                } else {
                    setMeasuredDimension(newW, hSize)
                }
            }
            else -> {
                setMeasuredDimension(newW, newW)
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (chartData.isEmpty()) {
            val textHeight = noDataPaint.fontMetrics.descent - noDataPaint.fontMetrics.ascent
            canvas.drawText("No data!", (width / 2).toFloat(), height / 2 + textHeight / 3, noDataPaint)
            return
        }

        radius = max(min(width, height) - strokeWidth.toInt() * 2, defaultRadius) / 2

        circleX = width / 2
        circleY = height / 2

        chartRect.left = circleX - strokeWidth / 2 - radius
        chartRect.top = circleY - strokeWidth / 2 - radius
        chartRect.right = circleX + strokeWidth / 2 + radius
        chartRect.bottom = circleY + strokeWidth / 2 + radius

        var startAngle = 0f
        var endAngle: Float

        chartData.forEachIndexed { i, it ->
            endAngle = if (i == chartData.size) {
                360f
            } else {
                startAngle + (it.value / allSum.toFloat() * 360)
            }
            drawChartPart(canvas, startAngle, endAngle, colors[i])
            startAngle = endAngle
        }

    }

    private fun drawChartPart(canvas: Canvas, startAngle: Float, endAngle: Float, color: Int) {
        categoryPaint.color = color

        canvas.drawArc(chartRect, startAngle - 90, endAngle - startAngle, false, categoryPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val tX = event.x.toDouble()
            val tY = event.y.toDouble()

            val r1 = radius
            val r2 = radius + strokeWidth

            val l = (tX - circleX) * (tX - circleX) + (tY - circleY) * (tY - circleY)

            if (l >= r1 * r1 && l <= r2 * r2) {
                var angle = Math.toDegrees(atan2(tY - circleY, tX - circleX)) + 90
                if (angle < 0)
                    angle += 360

                var startAngle = 0.0

                chartData.forEachIndexed { i, it ->
                    val endAngle: Double = if (i == chartData.size) {
                        360.0
                    } else {
                        startAngle + (it.value / allSum.toFloat() * 360)
                    }

                    if (angle > startAngle && angle < endAngle) {
                        onCategoryClickListener?.onClick(it)
                        return true
                    }

                    startAngle = endAngle
                }
            }
        }

        return false
    }

    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        bundle.putString(SavedState.keyOneCategory, Gson().toJson(data))
        val superState = super.onSaveInstanceState()
        return SavedState(superState, bundle)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is SavedState) {
            super.onRestoreInstanceState(state.superState)
            val type = object : TypeToken<List<Category.OneCategory>>() {}.type
            data = Gson().fromJson(state.oneCategoryList, type)
            setup(data)
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    fun setup(categories: List<Category.OneCategory>) {
        data = categories

        // Сортируем по убыванию
        val sorted = categories
            .sortedBy { it.value}
            .reversed()

        allSum = 0
        val list = mutableListOf<Category>()
        val otherCategory = Category.MultipleCategories(arrayListOf(), 0)

        sorted.forEachIndexed { i, cat ->
            if (i < maxCategories) {
                list.add(cat)
            } else {
                otherCategory.names.add(cat.name)
                otherCategory.value += cat.value
            }
            allSum += cat.value
        }

        if (otherCategory.value > 0) {
            list.add(otherCategory)
        }

        chartData = list
    }

    sealed class Category {

        abstract val value: Int

        data class OneCategory(val name: String, override val value: Int): Category()

        data class MultipleCategories(val names: MutableList<String>, override var value: Int): Category()

    }

    interface OnCategoryClickListener {
        fun onClick(category: Category)
    }

    private class SavedState : BaseSavedState {

        var oneCategoryList: String = ""

        constructor(superState: Parcelable?, bundle: Bundle) : super(superState) {
            oneCategoryList = bundle.getString(keyOneCategory, "")
        }

        constructor(parcel: Parcel) : super(parcel) {
            oneCategoryList = parcel.readString() ?: ""
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            super.writeToParcel(parcel, flags)
            parcel.writeString(oneCategoryList)
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {

            const val keyOneCategory = "keyOneCategory"

            override fun createFromParcel(parcel: Parcel): SavedState {
                return SavedState(parcel)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }

}
