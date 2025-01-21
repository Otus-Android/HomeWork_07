package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Parcelable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.pow

class LineChartView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    View(context, attrs), LineChartInterface {

    data class ExpensesInfoInternal(
        val category: String = "",
        val amount: Int = 0,
        val dayNumber: Int = 0,
        val dayOfMonth: Int = 0,
        val month: String = "",
        val year: Int = 0,
    )

    data class LineChartViewState(
        private val savedState: Parcelable?,
        val dataList: List<ExpensesInfoInternal>,
    ) : BaseSavedState(savedState), Parcelable

    private val paddings = context.dpToPx(20)
    private val textMargins = context.dpToPx(6)
    private val textsSize = context.spToPx(12)
    private val largeTextsSize = context.spToPx(16)
    private var textHeight = 0
    private var categoryExpensesText = ""
    private var categoryExpensesTextWidth = 0F
    private var categoryExpensesTextHeight = 0

    private var chartHeight = 0F
    private var chartTopPosition = 0F
    private var chartBottomPosition = 0F
    private var horizontalStep = 0
    private var verticalStep = 0
    private var numberOfDaysForChart = 0

    private val textsPaint = TextPaint().apply {
        color = Color.BLACK
        textSize = textsSize
        isAntiAlias = true
    }
    private val textRect = Rect()
    private val largeTextsPaint = TextPaint().apply {
        color = Color.BLACK
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textSize = largeTextsSize
        isAntiAlias = true
        setShadowLayer(5F, 3F, 3F, Color.GRAY)
    }

    private val axisPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 5F
        isAntiAlias = true
        setShadowLayer(5F, 3F, 3F, Color.GRAY)
    }

    private val gridLinesPaint = Paint().apply {
        color = Color.GRAY
        style = Paint.Style.STROKE
        pathEffect = DashPathEffect(floatArrayOf(10F, 10F), 0F)
        strokeWidth = 5F
        isAntiAlias = true
    }

    private val lineChartPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 5F
        isAntiAlias = true
        setShadowLayer(5F, 3F, 3F, Color.GRAY)
    }

    private val pointPaint = Paint().apply {
        color = Color.RED
        isAntiAlias = true
        setShadowLayer(5F, 3F, 3F, Color.GRAY)
    }

    private val expensesInfoListInternal = ArrayList<ExpensesInfoInternal>()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val initSize =
            minOf(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec))
        val maxAmount = expensesInfoListInternal.maxOf { it.amount }

        val maxAmountPeriod = 10.0.pow((maxAmount.toString().length - 1).toDouble())
        val maxAmountRoundedToUp = ceil(maxAmount.toDouble() / maxAmountPeriod) * maxAmountPeriod
        verticalStep = maxAmountRoundedToUp.toInt() / 4
        horizontalStep = (initSize - paddings * 2).toInt() / (numberOfDaysForChart - 1)

        textsPaint.getTextBounds(
            maxAmountRoundedToUp.toString(),
            0,
            maxAmountRoundedToUp.toString().length,
            textRect
        )
        textHeight = textRect.height()

        largeTextsPaint.getTextBounds(
            categoryExpensesText,
            0,
            categoryExpensesText.length,
            textRect
        )
        categoryExpensesTextWidth = largeTextsPaint.measureText(categoryExpensesText)
        categoryExpensesTextHeight = textRect.height()

        chartHeight =
            initSize - paddings * 2 - textHeight * 2 - categoryExpensesTextHeight - textMargins * 3
        chartTopPosition = paddings + textHeight + categoryExpensesTextHeight + textMargins * 2
        chartBottomPosition =
            paddings + textHeight + categoryExpensesTextHeight + textMargins * 2 + chartHeight

        setMeasuredDimension(initSize, initSize)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //Draw Header
        canvas.drawText(
            categoryExpensesText,
            paddings + horizontalStep * (numberOfDaysForChart - 1) / 2 - categoryExpensesTextWidth / 2,
            paddings + categoryExpensesTextHeight,
            largeTextsPaint
        )
        //DrawAxis
        canvas.drawLine(
            paddings,
            chartTopPosition,
            paddings,
            chartBottomPosition,
            axisPaint
        )
        canvas.drawLine(
            paddings,
            chartBottomPosition,
            paddings + horizontalStep * (numberOfDaysForChart - 1),
            chartBottomPosition,
            axisPaint
        )
        //Draw vertical steps and text
        for (i in 1..4) {
            canvas.drawLine(
                paddings,
                chartTopPosition + chartHeight * (i - 1) / 4,
                paddings + horizontalStep * (numberOfDaysForChart - 1),
                chartTopPosition + chartHeight * (i - 1) / 4,
                gridLinesPaint
            )
            canvas.drawText(
                (verticalStep * i).toString(),
                paddings + textMargins,
                chartTopPosition + chartHeight * (4 - i) / 4 - textMargins,
                textsPaint
            )
        }
        //Draw horizontal steps
        for (i in 1..<numberOfDaysForChart) {
            canvas.drawLine(
                paddings + horizontalStep * i,
                chartTopPosition,
                paddings + horizontalStep * i,
                chartBottomPosition,
                gridLinesPaint
            )
        }
        //Draw line chart
        var previousStopY = 0F
        expensesInfoListInternal.forEachIndexed { index, expenseInfoData ->
            val amountText = expenseInfoData.amount.toString()
            val dateText =
                if (index == 0 || expenseInfoData.month != expensesInfoListInternal[index - 1].month) {
                    "${expenseInfoData.dayOfMonth} ${expenseInfoData.month}"
                } else {
                    "${expenseInfoData.dayOfMonth}"
                }

            val currentStopY =
                chartBottomPosition - expenseInfoData.amount * chartHeight / (verticalStep * 4)
            val currentStopX = paddings + horizontalStep * index

            textsPaint.getTextBounds(dateText, 0, dateText.length, textRect)
            val textX = when (index) {
                0 -> 0F
                expensesInfoListInternal.size - 1 -> textsPaint.measureText(dateText)
                else -> textsPaint.measureText(dateText) / 2
            }

            canvas.drawText(
                dateText,
                currentStopX - textX,
                chartBottomPosition + textMargins + textHeight,
                textsPaint
            )

            if (index > 0) {
                canvas.drawLine(
                    paddings + horizontalStep * (index - 1),
                    previousStopY,
                    currentStopX,
                    currentStopY,
                    lineChartPaint
                )
            }
            if (expenseInfoData.amount > 0) {
                canvas.drawCircle(currentStopX, currentStopY, 7F, pointPaint)
                largeTextsPaint.getTextBounds(amountText, 0, amountText.length, textRect)
                val amountTextWidth = largeTextsPaint.measureText(amountText)
                canvas.drawText(
                    amountText,
                    currentStopX - amountTextWidth / 2,
                    currentStopY - textMargins,
                    largeTextsPaint
                )
            }

            previousStopY = currentStopY
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        val state = super.onSaveInstanceState()
        return LineChartViewState(
            state,
            expensesInfoListInternal
        )
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val lineChartViewState = state as? LineChartViewState
        super.onRestoreInstanceState(lineChartViewState?.superState ?: state)
        expensesInfoListInternal.clear()
        expensesInfoListInternal.addAll(lineChartViewState?.dataList ?: arrayListOf())
    }

    private fun getDay(time: Long): Int {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = time
        return calendar.get(Calendar.DAY_OF_MONTH)
    }

    private fun getMonth(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("MMM", Locale.getDefault())
        return format.format(date)
    }

    private fun getYear(time: Long): Int {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = time
        return calendar.get(Calendar.YEAR)
    }

    override fun setDataAndColor(expensesInfoList: List<ExpensesInfoItem>, color: Int) {
        lineChartPaint.color = color
        pointPaint.color = color
        expensesInfoListInternal.clear()
        categoryExpensesText = String.format(
            context.resources.getString(R.string.category_expenses_text),
            expensesInfoList[0].category
        )
        val minTimeForChart = expensesInfoList.minOf { it.time } - 86400
        val minTimeInDays = (expensesInfoList.minOf { it.time } / 86400).toInt()
        val maxTimeInDays = (expensesInfoList.maxOf { it.time } / 86400).toInt()
        numberOfDaysForChart = maxTimeInDays - minTimeInDays + 3

        var previousDayNumber = 0
        expensesInfoList.forEachIndexed { index, expensesInfoItem ->
            if (index == 0) {
                expensesInfoListInternal.add(
                    ExpensesInfoInternal(
                        category = expensesInfoItem.category,
                        amount = 0,
                        dayNumber = 0,
                        dayOfMonth = getDay(minTimeForChart * 1000),
                        month = getMonth(minTimeForChart * 1000),
                        year = getYear(minTimeForChart * 1000)
                    )
                )
            }

            val dayNumber = (expensesInfoItem.time / 86400 - minTimeInDays).toInt() + 1

            while (previousDayNumber + 1 < dayNumber) {
                val time = (previousDayNumber + minTimeInDays) * 86400000L
                expensesInfoListInternal.add(
                    ExpensesInfoInternal(
                        category = expensesInfoItem.category,
                        amount = 0,
                        dayNumber = ++previousDayNumber,
                        dayOfMonth = getDay(time),
                        month = getMonth(time),
                        year = getYear(time)
                    )
                )
            }
            val indexExpensesInfoListInternalItem =
                expensesInfoListInternal.indexOfFirst {
                    it.dayNumber ==
                            dayNumber
                            && it.category == expensesInfoItem.category
                }
            if (indexExpensesInfoListInternalItem >= 0) {
                expensesInfoListInternal[indexExpensesInfoListInternalItem] =
                    expensesInfoListInternal[indexExpensesInfoListInternalItem].copy(amount = expensesInfoListInternal[indexExpensesInfoListInternalItem].amount + expensesInfoItem.amount)
            } else {
                expensesInfoListInternal.add(
                    ExpensesInfoInternal(
                        category = expensesInfoItem.category,
                        amount = expensesInfoItem.amount,
                        dayNumber = dayNumber,
                        dayOfMonth = getDay(expensesInfoItem.time * 1000),
                        month = getMonth(expensesInfoItem.time * 1000),
                        year = getYear(expensesInfoItem.time * 1000)
                    )
                )
            }
            if (index == expensesInfoList.size - 1) {
                val time = (dayNumber + minTimeInDays) * 86400000L
                expensesInfoListInternal.add(
                    ExpensesInfoInternal(
                        category = expensesInfoItem.category,
                        amount = 0,
                        dayNumber = dayNumber + 1,
                        dayOfMonth = getDay(time),
                        month = getMonth(time),
                        year = getYear(time)
                    )
                )
            }
            previousDayNumber = dayNumber
        }
    }
}