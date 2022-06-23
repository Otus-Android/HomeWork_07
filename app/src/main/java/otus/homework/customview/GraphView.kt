package otus.homework.customview

import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class GraphView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val widthDefault = (200 * context.resources.displayMetrics.density).toInt()
    private val heightDefault = (450 * context.resources.displayMetrics.density).toInt()

    private var heightMax = 1
    private var categories: LinkedHashMap<String, Category> = LinkedHashMap()
    var startDate: Int = 0
    var endDate: Int = 1
    var rangeDate = 1
    val sdf = SimpleDateFormat("dd", Locale.getDefault())
    var items: ArrayList<Item>? = null

    private var paint = Paint().apply {
        style = Paint.Style.STROKE
        flags = Paint.ANTI_ALIAS_FLAG
        color = Color.WHITE
        strokeWidth = 2 * context.resources.displayMetrics.density
    }

    private var path = Path()

    fun setCoordinate(items: ArrayList<Item>?) {
        items?.let {
            val dates = it.groupBy { it.date }
            startDate = dates.keys.minOf { sdf.format(it).toInt() }
            endDate = dates.keys.maxOf { sdf.format(it).toInt() }
            rangeDate = (endDate - startDate)
        }
        val categories = items?.groupBy { it.category }
        categories?.values?.forEach{
            val sum = it.sumOf { it.amount }
            if (heightMax < sum) {
                heightMax = sum
            }
        }
    }

    fun setItems(items: ArrayList<Item>?, color: Int?) {
        categories.clear()
        this.items = items
        items?.sortedBy { it.date }?.forEach {
            if (categories.containsKey(it.category)) {
                val category = categories.get(it.category)
                category!!.items?.add(it)
                category.totalAmount = category.totalAmount + it.amount
            } else {
                categories.put(
                    it.category!!,
                    Category(
                        it.category,
                        arrayListOf(it),
                        it.amount,
                        color ?: PieChartView.colors.get(categories.size)
                    )
                )
            }
        }
        requestLayout()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val modeWidth = MeasureSpec.getMode(widthMeasureSpec)
        val modeHeight = MeasureSpec.getMode(heightMeasureSpec)
        val widthMeasure = MeasureSpec.getSize(widthMeasureSpec)
        val heightMeasure = MeasureSpec.getSize(heightMeasureSpec)
        val width = measureSize(modeWidth, widthMeasure, widthDefault)
        val height = measureSize(modeHeight, heightMeasure, heightDefault)
        setMeasuredDimension(width, height)
    }

    var dateByValue = TreeMap<Int, Int>()

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        paint.color = Color.BLACK
        canvas?.drawPaint(paint)

        categories.values.forEach {
            val date = it.items?.groupBy { it.date }
            dateByValue.clear()

            date?.forEach {
                dateByValue.put(sdf.format(it.key).toInt(), it.value.sumOf { it.amount })
            }
            path.reset()

            path.moveTo(0f,
                (((dateByValue.get(dateByValue.keys.minOf { it })?.toDouble()
                    ?: 1.0) / heightMax.toDouble()) * height).toFloat())

            var lastY = 0F
            for (i in startDate until endDate) {
                val x = (((i - startDate.toDouble()) / rangeDate.toDouble()) * width).toFloat()

                var y = if (dateByValue.containsKey(i)) {
                    Log.d("GraphViewcalculatey", "category ${it.name}dateByValue.get(i) ${dateByValue.get(i)}  heightMax.toDouble() ${heightMax.toDouble()} height $height")
                    (((dateByValue.get(i) ?: 1).toDouble() / heightMax.toDouble()) * height).toFloat()
                } else {
                    0F
                }
                lastY += y
                Log.d("GraphView", "GraphViewx $x  rangeDate $rangeDate width $width")
                Log.d("GraphView", "GraphViewy ${height - lastY}  heightMax $heightMax height $height")
                path.lineTo(x, (height - lastY))
            }
            path.lineTo(width.toFloat(), (height - lastY))

            paint.color = it.color
            canvas?.drawPath(path, paint)
        }
    }

    private fun measureSize(mode: Int, size: Int, sizeMax: Int) = when (mode) {
        MeasureSpec.EXACTLY -> {
            size
        }
        MeasureSpec.AT_MOST -> {
            if (sizeMax <= size) {
                sizeMax
            } else {
                size
            }
        }
        MeasureSpec.UNSPECIFIED -> {
            sizeMax
        }
        else -> {
            size
        }
    }

    override fun onSaveInstanceState(): Parcelable =
        Bundle().apply {
            putInt("heightMax", heightMax)
            putInt("startDate", startDate)
            putInt("endDate", endDate)
            putInt("rangeDate", rangeDate)
            putParcelableArrayList("items", items)
            putParcelable("superState", super.onSaveInstanceState())
        }


    override fun onRestoreInstanceState(state: Parcelable?) = super.onRestoreInstanceState(
        if (state is Bundle) {
            heightMax = state.getInt("heightMax")
            startDate = state.getInt("startDate")
            endDate = state.getInt("endDate")
            rangeDate = state.getInt("rangeDate")
            items = state.getParcelableArrayList("items")
            state.getParcelable("superState")
        } else {
            state
        }
    )

}