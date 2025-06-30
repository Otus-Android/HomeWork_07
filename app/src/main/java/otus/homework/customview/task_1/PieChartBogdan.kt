package otus.homework.customview.task_1

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.content.withStyledAttributes
import otus.homework.customview.R
import otus.homework.customview.task_1.model.Category
import otus.homework.customview.task_1.model.Data
import kotlin.math.*

class PieChartBogdan @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val preparedDataForCanvas: MutableSet<CategorySector> = mutableSetOf()

    //categories paint
    private lateinit var healthCategoryColor: Paint
    private lateinit var transportCategoryColor: Paint
    private lateinit var productsCategoryColor: Paint
    private lateinit var alcoholCategoryColor: Paint
    private lateinit var deliveryCategoryColor: Paint
    private lateinit var sportCategoryColor: Paint
    private lateinit var hotelsCategoryColor: Paint
    private lateinit var cinemaCategoryColor: Paint
    private lateinit var clothesCategoryColor: Paint
    private lateinit var cafesAndRestaurantsCategoryColor: Paint

    private val rect = RectF()

    private val gestureDetector: GestureDetector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent?): Boolean {
                e?.let {
                    findOutCategory(it)
                }
                return true
            }
        })

    var pressOnSector: ((Category) -> Unit) = {}

    init {
        initData()

        context.withStyledAttributes(attrs, R.styleable.PieChartBogdan) {
            val healthCategoryColor =
                getColor(R.styleable.PieChartBogdan_healthCategoryColor, Color.MAGENTA)
            val transportCategoryColor =
                getColor(R.styleable.PieChartBogdan_transportCategoryColor, Color.YELLOW)
            val productsCategoryColor =
                getColor(R.styleable.PieChartBogdan_productsCategoryColor, Color.RED)
            val alcoholCategoryColor =
                getColor(R.styleable.PieChartBogdan_alcoholCategoryColor, Color.BLUE)
            val deliveryCategoryColor =
                getColor(R.styleable.PieChartBogdan_deliveryCategoryColor, Color.GREEN)
            val sportCategoryColor =
                getColor(R.styleable.PieChartBogdan_sportCategoryColor, Color.DKGRAY)
            val hotelsCategoryColor =
                getColor(R.styleable.PieChartBogdan_hotelsCategoryColor, Color.CYAN)
            val cinemaCategoryColor =
                getColor(R.styleable.PieChartBogdan_cinemaCategoryColor, Color.BLACK)
            val clothesCategoryColor =
                getColor(R.styleable.PieChartBogdan_clothesCategoryColor, Color.LTGRAY)
            val cafesAndRestaurantsCategoryColor =
                getColor(R.styleable.PieChartBogdan_cafesAndRestaurantsCategoryColor, Color.GRAY)

            setupColors(
                healthCategoryColor,
                transportCategoryColor,
                productsCategoryColor,
                alcoholCategoryColor,
                deliveryCategoryColor,
                sportCategoryColor,
                hotelsCategoryColor,
                cinemaCategoryColor,
                clothesCategoryColor,
                cafesAndRestaurantsCategoryColor,
            )
        }

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val wMode = MeasureSpec.getMode(widthMeasureSpec)
        val wSize = MeasureSpec.getSize(widthMeasureSpec)
        val hSize = MeasureSpec.getSize(heightMeasureSpec)

        when (wMode) {
            MeasureSpec.EXACTLY -> {
                setMeasuredDimension(wSize, hSize)
            }

            MeasureSpec.AT_MOST -> {
                setMeasuredDimension(wSize, min(wSize, hSize))
            }

            MeasureSpec.UNSPECIFIED -> {
                setMeasuredDimension(UNSPECIFIED_SIZE, UNSPECIFIED_SIZE)
            }
        }
    }

    override fun onDraw(canvas: Canvas?) {
        rect.set(0f, 0f, width.toFloat(), width.toFloat())

        preparedDataForCanvas.forEach { sector ->
            if (sector.sweepAngle > 0) {
                canvas?.drawArc(
                    rect,
                    sector.startAngle,
                    sector.sweepAngle,
                    true,
                    sector.category.getPaint()
                )
            }
        }


    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        gestureDetector.onTouchEvent(event)
        return true
    }

    fun setData(dataList: List<Data>) {
        preparedDataForCanvas.clear()
        var tempAngle = 0f
        val oneDegree = dataList.sumOf { it.amount } / FULL_TURN
        dataList.groupBy { it.category }.forEach { (category, data) ->
            val sweepAngle = data.sumOf { it.amount } / oneDegree
            preparedDataForCanvas.add(
                CategorySector(
                    category = category,
                    startAngle = tempAngle,
                    sweepAngle = sweepAngle
                )
            )
            tempAngle += data.sumOf { it.amount } / oneDegree
        }
        requestLayout()
        invalidate()
    }

    private fun setupColors(
        healthCategoryColor: Int,
        transportCategoryColor: Int,
        productsCategoryColor: Int,
        alcoholCategoryColor: Int,
        deliveryCategoryColor: Int,
        sportCategoryColor: Int,
        hotelsCategoryColor: Int,
        cinemaCategoryColor: Int,
        clothesCategoryColor: Int,
        cafesAndRestaurantsCategoryColor: Int
    ) {

        this.healthCategoryColor = Paint().apply {
            color = healthCategoryColor
            style = Paint.Style.FILL
        }
        this.transportCategoryColor = Paint().apply {
            color = transportCategoryColor
            style = Paint.Style.FILL
        }
        this.productsCategoryColor = Paint().apply {
            color = productsCategoryColor
        }
        this.alcoholCategoryColor = Paint().apply {
            color = alcoholCategoryColor
        }
        this.deliveryCategoryColor = Paint().apply {
            color = deliveryCategoryColor
            style = Paint.Style.FILL
        }
        this.sportCategoryColor = Paint().apply {
            color = sportCategoryColor
            style = Paint.Style.FILL
        }
        this.hotelsCategoryColor = Paint().apply {
            color = hotelsCategoryColor
            style = Paint.Style.FILL
        }
        this.cinemaCategoryColor = Paint().apply {
            color = cinemaCategoryColor
            style = Paint.Style.FILL
        }
        this.clothesCategoryColor = Paint().apply {
            color = clothesCategoryColor
            style = Paint.Style.FILL
        }
        this.cafesAndRestaurantsCategoryColor = Paint().apply {
            color = cafesAndRestaurantsCategoryColor
            style = Paint.Style.FILL
        }
    }

    private fun initData() {
        var tempAngle = 0f
        Category.values().forEach { category ->
            val sweepAngle = FULL_TURN / Category.values().size
            preparedDataForCanvas.add(
                CategorySector(
                    category = category,
                    startAngle = tempAngle,
                    sweepAngle = sweepAngle
                )
            )
            tempAngle += sweepAngle
        }
    }

    private fun Category.getPaint() = when (this) {
        Category.HEALTH -> healthCategoryColor
        Category.TRANSPORT -> transportCategoryColor
        Category.PRODUCTS -> productsCategoryColor
        Category.CAFES_AND_RESTAURANTS -> cafesAndRestaurantsCategoryColor
        Category.ALCOHOL -> alcoholCategoryColor
        Category.DELIVERY -> deliveryCategoryColor
        Category.SPORT -> sportCategoryColor
        Category.HOTELS -> hotelsCategoryColor
        Category.CINEMA -> cinemaCategoryColor
        Category.CLOTHES -> clothesCategoryColor
    }

    private fun findOutCategory(event: MotionEvent) {
        val centerX = width / 2
        val centerY = centerX

        val hipotenusa = sqrt((centerX - event.x).pow(2) + (centerY - event.y).pow(2))

        if(hipotenusa > centerX) return

        val eventAngle = when {
            event.x > centerX && event.y > centerY -> {
                abs(asin((centerY - event.y) / hipotenusa) * NINETY * 2 / PI)
            }

            event.x < centerX && event.y > centerY -> {
               NINETY * 2 - abs(asin((centerY - event.y) / hipotenusa) * NINETY * 2 / PI)
            }

            event.x < centerX && event.y < centerY -> {
                abs(asin((centerY - event.y) / hipotenusa) * NINETY * 2 / PI) + NINETY * 2
            }

            event.x > centerX && event.y < centerY -> {
               NINETY * 4 - abs(asin((centerY - event.y) / hipotenusa) * NINETY * 2 / PI)
            }

            else -> error("Invalid angle")
        }

                preparedDataForCanvas.forEach { sector ->
                    if (eventAngle in sector.startAngle..sector.endAngle) {
                        pressOnSector(sector.category)
                    }
                }
    }

    inner class CategorySector(
        val category: Category,
        val startAngle: Float,
        val sweepAngle: Float,
    ) {
        val endAngle = startAngle + sweepAngle

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as CategorySector
            return category == other.category && startAngle == other.startAngle && sweepAngle == other.sweepAngle
        }

        override fun hashCode(): Int {
            return category.hashCode() + sweepAngle.hashCode() + startAngle.hashCode()
        }
    }

    companion object {
        private const val FULL_TURN = 360f
        private const val NINETY = 90
        private const val UNSPECIFIED_SIZE = 3000
    }
}