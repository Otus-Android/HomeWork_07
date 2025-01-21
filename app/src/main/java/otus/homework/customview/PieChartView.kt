package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.os.Parcelable
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.sqrt

class PieChartView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    View(context, attrs), PieChartInterface {

    data class ExpensesInfoInternal(
        val category: String = "",
        val amount: Int = 0,
        val percent: Int = 0,
        val angle: Float = 0F,
        val textHeight: Int = 0,
        @ColorInt
        val color: Int = 0,
    )

    data class PieChartViewState(
        private val savedState: Parcelable?,
        val dataList: List<ExpensesInfoInternal>,
    ) : BaseSavedState(savedState), Parcelable

    private var onClickListener: OnCategoryClickListener? = null

    private val paddings = context.dpToPx(20)
    private val textMargins = context.dpToPx(6)
    private val amountTextMargins = context.dpToPx(20)
    private val textsSize = context.spToPx(16)
    private val amountTextSize = context.spToPx(32)
    private val smallToFullArcRadiusDiff = context.dpToPx(4).toInt()
    private val circleSectionSpace = 2F

    private var pieChartHeight = 0F
    private var pieChartFullRadius = 0F
    private val pieChartSmallRadius: Float
        get() = pieChartFullRadius - smallToFullArcRadiusDiff
    private var smallCircleRadius = context.dpToPx(8)
    private val smallArcPaddings = paddings + smallToFullArcRadiusDiff
    private var arcStrokeWidth = context.dpToPx(72)
    private val fullArcRect = RectF()
    private val smallArcRect = RectF()
    private var centerX = 0F
    private var centerY = 0F
    private var sumAmount = 0
    private val amountTextRect = Rect()

    private val arcPaint: Paint = Paint().apply {
        style = Paint.Style.STROKE
        isAntiAlias = true
        isDither = true
        strokeWidth = arcStrokeWidth
        setShadowLayer(15F, 5F, 5F, Color.GRAY)
    }
    private val smallCirclePaint: Paint = Paint().apply {
        isAntiAlias = true
        isDither = true
        setShadowLayer(5F, 3F, 3F, Color.GRAY)
    }
    private val textPaint = TextPaint().apply {
        color = Color.BLACK
        textSize = textsSize
        isAntiAlias = true
    }
    private val amountTextPaint = TextPaint().apply {
        color = Color.BLACK
        textSize = amountTextSize
        isAntiAlias = true
    }


    private val expensesInfoListInternal = ArrayList<ExpensesInfoInternal>()
    private val textList = ArrayList<StaticLayout>()
    private val colorList = resources.getIntArray(R.array.colors)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val initWidth = MeasureSpec.getSize(widthMeasureSpec)
        val initHeight = MeasureSpec.getSize(heightMeasureSpec)

        textList.clear()

        var textHeight = expensesInfoListInternal.size * textMargins + paddings
        var textHeightForTouchDetect = 0
        expensesInfoListInternal.forEachIndexed { index, expensesInfoInternal ->
            val text = String.format(
                context.resources.getString(R.string.text_description),
                expensesInfoInternal.amount,
                expensesInfoInternal.percent,
                expensesInfoInternal.category
            )
            val textLayout = getStaticLayout(text, initWidth - (paddings * 2).toInt())
            textHeight += textLayout.height
            textHeightForTouchDetect += textLayout.height + textMargins.toInt()
            expensesInfoListInternal[index] =
                expensesInfoInternal.copy(textHeight = textHeightForTouchDetect)
            textList.add(textLayout)
        }

        pieChartHeight = if (initHeight / 2 < textHeight) initHeight - textHeight
        else (initHeight / 2).toFloat()
        val viewHeight = pieChartHeight + textHeight.toInt()

        pieChartFullRadius =
            (min(pieChartHeight, initWidth.toFloat()) - paddings * 2 - arcStrokeWidth) / 2
        arcStrokeWidth = pieChartFullRadius / 2.1F

        smallCircleRadius = (textHeight - paddings) / expensesInfoListInternal.size / 3

        centerX = smallArcPaddings + pieChartSmallRadius + arcStrokeWidth / 2
        centerY = smallArcPaddings + pieChartSmallRadius + arcStrokeWidth / 2

        with(fullArcRect) {
            top = paddings + arcStrokeWidth / 2
            bottom = paddings + pieChartFullRadius * 2 + arcStrokeWidth / 2
            left = paddings + arcStrokeWidth / 2
            right = paddings + pieChartFullRadius * 2 + arcStrokeWidth / 2
        }
        with(smallArcRect) {
            top = smallArcPaddings + arcStrokeWidth / 2
            bottom = smallArcPaddings + pieChartSmallRadius * 2 + arcStrokeWidth / 2
            left = smallArcPaddings + arcStrokeWidth / 2
            right = smallArcPaddings + pieChartSmallRadius * 2 + arcStrokeWidth / 2
        }

        setMeasuredDimension(initWidth, viewHeight.toInt())
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawText(canvas)
        drawPieChart(canvas)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val clickedCategoryWithColor = getClickedCategoryWithColor(event.x, event.y)
                if (clickedCategoryWithColor.first != NO_CATEGORY) {
                    onClickListener?.onClick(clickedCategoryWithColor)
                }
            }
        }
        return true
    }

    override fun onSaveInstanceState(): Parcelable {
        val state = super.onSaveInstanceState()
        return PieChartViewState(state, expensesInfoListInternal)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val pieChartViewState = state as? PieChartViewState
        super.onRestoreInstanceState(pieChartViewState?.superState ?: state)
        expensesInfoListInternal.clear()
        expensesInfoListInternal.addAll(pieChartViewState?.dataList ?: arrayListOf())
    }

    private fun getClickedCategoryWithColor(xPos: Float, yPos: Float): Pair<String, Int> {
        val x = xPos - centerX
        val y = yPos - centerY

        val radius = sqrt(x * x + y * y)
        if (radius > pieChartSmallRadius - arcStrokeWidth / 2 && radius < pieChartFullRadius + arcStrokeWidth / 2) {
            var angle = Math.toDegrees(atan2(y.toDouble(), x.toDouble()) + Math.PI / 2)
            angle = if (angle < 0) angle + 360 else angle

            var startAngle = 0F
            expensesInfoListInternal.forEach { expensesInfoInternal ->
                val endAngle = startAngle + expensesInfoInternal.angle
                if (angle in startAngle..endAngle) return expensesInfoInternal.category to expensesInfoInternal.color
                startAngle = endAngle
            }
        } else if (radius < pieChartSmallRadius - arcStrokeWidth / 2) {
            return NO_CATEGORY to 0
        }

        if (yPos > pieChartHeight + textMargins) {
            expensesInfoListInternal.forEach {
                if (yPos < pieChartHeight + textMargins + it.textHeight) return it.category to it.color
            }
        }

        return NO_CATEGORY to 0
    }

    private fun drawPieChart(canvas: Canvas) {
        var startAngle = -90F
        expensesInfoListInternal.forEachIndexed { index, expensesInfoInternal ->
            if (index == 0 || expensesInfoInternal.amount == expensesInfoListInternal[0].amount) {
                canvas.drawArc(
                    fullArcRect,
                    startAngle,
                    expensesInfoInternal.angle - circleSectionSpace,
                    false,
                    arcPaint.apply {
                        color = expensesInfoInternal.color
                        strokeWidth = arcStrokeWidth + smallToFullArcRadiusDiff * 2
                    })

            } else {
                canvas.drawArc(
                    smallArcRect,
                    startAngle,
                    expensesInfoInternal.angle - circleSectionSpace,
                    false,
                    arcPaint.apply {
                        color = expensesInfoInternal.color
                        strokeWidth = arcStrokeWidth
                    })
            }
            startAngle += expensesInfoInternal.angle
        }
    }

    private fun getStaticLayout(text: CharSequence, viewWidth: Int): StaticLayout {
        return StaticLayout.Builder
            .obtain(text, 0, text.length, textPaint, viewWidth)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setTextDirection(TextDirectionHeuristics.LOCALE)
            .setLineSpacing(0F, 1F)
            .build()
    }

    private fun drawText(canvas: Canvas) {
        var textY = pieChartHeight + textMargins
        textList.forEachIndexed { index, it ->
            it.draw(canvas, paddings + smallCircleRadius * 2 + textMargins, textY)
            canvas.drawCircle(
                paddings + smallCircleRadius,
                textY + it.height / 2,
                smallCircleRadius,
                smallCirclePaint.apply {
                    color = expensesInfoListInternal[index].color
                }
            )
            textY += it.height + textMargins
        }

        var amountText = "$sumAmount"
        amountTextPaint.getTextBounds(amountText, 0, amountText.length, amountTextRect)
        var amountTextWidth = amountTextPaint.measureText(amountText)
        var amountTextHeight = amountTextRect.height()
        canvas.drawText(
            amountText,
            centerX - amountTextWidth / 2,
            centerY + amountTextHeight / 2 - amountTextMargins,
            amountTextPaint
        )
        amountText = context.resources.getString(R.string.currency)
        amountTextPaint.getTextBounds(amountText, 0, amountText.length, amountTextRect)
        amountTextWidth = amountTextPaint.measureText(amountText)
        amountTextHeight = amountTextRect.height()
        canvas.drawText(
            amountText,
            centerX - amountTextWidth / 2,
            centerY + amountTextHeight / 2 + amountTextMargins,
            amountTextPaint
        )
    }

    override fun setOnCategoryClickListener(onCategoryClickListener: OnCategoryClickListener) {
        this.onClickListener = onCategoryClickListener
    }

    override fun setData(expensesInfoList: List<ExpensesInfoItem>) {
        expensesInfoListInternal.clear()
        expensesInfoList.forEach { expensesInfoItem ->
            val index =
                expensesInfoListInternal.indexOfFirst { it.category == expensesInfoItem.category }
            if (index >= 0) {
                expensesInfoListInternal[index] =
                    expensesInfoListInternal[index].copy(amount = expensesInfoListInternal[index].amount + expensesInfoItem.amount)
            } else expensesInfoListInternal.add(
                ExpensesInfoInternal(
                    category = expensesInfoItem.category,
                    amount = expensesInfoItem.amount
                )
            )
        }
        expensesInfoListInternal.sortByDescending { it.amount }
        sumAmount = expensesInfoListInternal.sumOf { it.amount }

        var value = 0.0
        var roundedValue = 0L
        var prevRoundedValue: Long
        expensesInfoListInternal.forEachIndexed { index, expensesInfoItem ->
            value += expensesInfoListInternal[index].amount * 100.0 / sumAmount
            prevRoundedValue = roundedValue
            roundedValue = Math.round(value)
            expensesInfoListInternal[index] = expensesInfoItem.copy(
                percent = (roundedValue - prevRoundedValue).toInt(),
                angle = 360F * (roundedValue - prevRoundedValue) / 100,
                color = colorList[index]
            )
        }
    }

    companion object {
        const val NO_CATEGORY = ""
    }

    interface OnCategoryClickListener {
        fun onClick(categoryWithColor: Pair<String, Int>)
    }
}