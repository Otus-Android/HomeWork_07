package otus.homework.customview.pieChart

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Parcelable
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristic
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import otus.homework.customview.R
import otus.homework.customview.dpToPx
import otus.homework.customview.draw
import otus.homework.customview.spToPx
import kotlin.math.atan2

class PieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val DEFAULT_MARGIN_TEXT_1 = 2
        private const val DEFAULT_MARGIN_TEXT_2 = 10
        private const val DEFAULT_MARGIN_TEXT_3 = 2
        private const val DEFAULT_MARGIN_SMALL_CIRCLE = 12

        private const val TEXT_WIDTH_PERCENT = 0.40
        private const val CIRCLE_WIDTH_PERCENT = 0.50
        const val DEFAULT_VIEW_SIZE_HEIGHT = 150
        const val DEFAULT_VIEW_SIZE_WIDTH = 250
    }

    private var marginTextFirst: Float = context.dpToPx(DEFAULT_MARGIN_TEXT_1)
    private var marginTextSecond: Float = context.dpToPx(DEFAULT_MARGIN_TEXT_2)
    private var marginTextThird: Float = context.dpToPx(DEFAULT_MARGIN_TEXT_3)
    private var marginSmallCircle: Float = context.dpToPx(DEFAULT_MARGIN_SMALL_CIRCLE)
    private val marginText: Float = marginTextFirst + marginTextSecond
    private val circleRect = RectF()
    private var circleStrokeWidth: Float = context.dpToPx(6)
    private var circleRadius: Float = 0F
    private var circlePadding: Float = context.dpToPx(8)
    private var circlePaintRoundSize: Boolean = true
    private var circleSectionSpace: Float = 3F
    private var circleCenterX: Float = 0F
    private var circleCenterY: Float = 0F
    private var numberTextPaint: TextPaint = TextPaint()
    private var descriptionTextPain: TextPaint = TextPaint()
    private var amountTextPaint: TextPaint = TextPaint()
    private var textStartX: Float = 0F
    private var textStartY: Float = 0F
    private var textHeight: Int = 0
    private var textCircleRadius: Float = context.dpToPx(4)
    private var textAmountStr: String = ""
    private var textAmountY: Float = 0F
    private var textAmountXNumber: Float = 0F
    private var textAmountXDescription: Float = 0F
    private var textAmountYDescription: Float = 0F
    private var totalAmount: Int = 0
    private var pieChartColors: List<String> = listOf()
    private var percentageCircleList: List<PieChartModel> = listOf()
    private var textRowList: MutableList<StaticLayout> = mutableListOf()
    private var dataList: List<PieChartData> = listOf()
    private var animationSweepAngle: Int = 0
    private var selectedIndex: Int = -1 // Индекс выбранного сектора

    init {
        var textAmountSize: Float = context.spToPx(22)
        var textNumberSize: Float = context.spToPx(20)
        var textDescriptionSize: Float = context.spToPx(14)
        var textAmountColor: Int = Color.GRAY
        var textNumberColor: Int = Color.GRAY
        var textDescriptionColor: Int = Color.GRAY

        if (attrs != null) {
            val typeArray = context.obtainStyledAttributes(attrs, R.styleable.PieChartView)

            val colorResId = typeArray.getResourceId(R.styleable.PieChartView_pieChartColors, 0)
            pieChartColors = typeArray.resources.getStringArray(colorResId).toList()

            marginTextFirst = typeArray.getDimension(R.styleable.PieChartView_pieChartMarginTextFirst, marginTextFirst)
            marginTextSecond = typeArray.getDimension(R.styleable.PieChartView_pieChartMarginTextSecond, marginTextSecond)
            marginTextThird = typeArray.getDimension(R.styleable.PieChartView_pieChartMarginTextThird, marginTextThird)
            marginSmallCircle = typeArray.getDimension(R.styleable.PieChartView_pieChartMarginSmallCircle, marginSmallCircle)

            circleStrokeWidth = typeArray.getDimension(R.styleable.PieChartView_pieChartCircleStrokeWidth, circleStrokeWidth)
            circlePadding = typeArray.getDimension(R.styleable.PieChartView_pieChartCirclePadding, circlePadding)
            circlePaintRoundSize = typeArray.getBoolean(R.styleable.PieChartView_pieChartCirclePaintRoundSize, circlePaintRoundSize)
            circleSectionSpace = typeArray.getFloat(R.styleable.PieChartView_pieChartCircleSectionSpace, circleSectionSpace)

            textCircleRadius = typeArray.getDimension(R.styleable.PieChartView_pieChartTextCircleRadius, textCircleRadius)
            textAmountSize = typeArray.getDimension(R.styleable.PieChartView_pieChartTextAmountSize, textAmountSize)
            textNumberSize = typeArray.getDimension(R.styleable.PieChartView_pieChartTextNumberSize, textNumberSize)
            textDescriptionSize = typeArray.getDimension(R.styleable.PieChartView_pieChartTextDescriptionSize, textDescriptionSize)
            textDescriptionColor = typeArray.getColor(R.styleable.PieChartView_pieChartTextDescriptionColor, textDescriptionColor)
            textAmountStr = typeArray.getString(R.styleable.PieChartView_pieChartTextAmount) ?: ""

            typeArray.recycle()
        }

        circlePadding += circleStrokeWidth

        initPains(amountTextPaint, textAmountSize, textAmountColor)
        initPains(numberTextPaint, textNumberSize, textNumberColor)
        initPains(descriptionTextPain, textDescriptionSize, textDescriptionColor, true)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        textRowList.clear()

        val initSizeWidth = resolveDefaultSize(widthMeasureSpec, DEFAULT_VIEW_SIZE_WIDTH)

        val textTextWidth = (initSizeWidth * TEXT_WIDTH_PERCENT)
        val initSizeHeight = calculateViewHeight(heightMeasureSpec, textTextWidth.toInt())

        textStartX = initSizeWidth - textTextWidth.toFloat()
        textStartY = initSizeHeight.toFloat() / 2 - textHeight / 2

        calculateCircleRadius(initSizeWidth, initSizeHeight)

        setMeasuredDimension(initSizeWidth, initSizeHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawCircle(canvas)
        drawText(canvas)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val pieChartState = state as? PieChartState
        super.onRestoreInstanceState(pieChartState?.superState ?: state)

        dataList = pieChartState?.dataList ?: listOf()
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        return PieChartState(superState, dataList)
    }

    fun setDataChart(list: List<PieChartData>) {
        dataList = list
        calculatePercentageOfData()
    }

    fun startAnimation() {
        val animator = ValueAnimator.ofInt(0, 360).apply {
            duration = 1500
            interpolator = FastOutSlowInInterpolator()
            addUpdateListener { valueAnimator ->
                animationSweepAngle = valueAnimator.animatedValue as Int
                invalidate()
            }
        }
        animator.start()
    }

    private fun drawCircle(canvas: Canvas) {
        for ((index, percent) in percentageCircleList.withIndex()) {
            val paint = if (index == selectedIndex) {
                Paint(percent.paint).apply {
                    strokeWidth *= 2f
                }
            } else {
                percent.paint
            }
            if (animationSweepAngle > percent.percentToStartAt + percent.percentOfCircle) {
                canvas.drawArc(circleRect, percent.percentToStartAt, percent.percentOfCircle, false, paint)
            } else if (animationSweepAngle > percent.percentToStartAt) {
                canvas.drawArc(circleRect, percent.percentToStartAt, animationSweepAngle - percent.percentToStartAt, false, paint)
            }
        }
    }

    private fun drawText(canvas: Canvas) {
        var textBuffY = textStartY
        textRowList.forEachIndexed { index, staticLayout ->
            if (index % 2 == 0) {
                staticLayout.draw(canvas, textStartX + marginSmallCircle + textCircleRadius, textBuffY)
                canvas.drawCircle(
                    textStartX + marginSmallCircle / 2,
                    textBuffY + staticLayout.height / 2 + textCircleRadius / 2,
                    textCircleRadius,
                    Paint().apply {
                        color = Color.parseColor(pieChartColors[(index / 2) % pieChartColors.size])
                    }
                )
                textBuffY += staticLayout.height + marginTextFirst
            } else {
                staticLayout.draw(canvas, textStartX, textBuffY)
                textBuffY += staticLayout.height + marginTextSecond
            }
        }

        canvas.drawText(totalAmount.toString(), textAmountXNumber, textAmountY, amountTextPaint)
        canvas.drawText(textAmountStr, textAmountXDescription, textAmountYDescription, descriptionTextPain)
    }

    private fun initPains(textPaint: TextPaint, textSize: Float, textColor: Int, isDescription: Boolean = false) {
        textPaint.color = textColor
        textPaint.textSize = textSize
        textPaint.isAntiAlias = true

        if (!isDescription) textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private fun resolveDefaultSize(spec: Int, defValue: Int): Int {
        return when (MeasureSpec.getMode(spec)) {
            MeasureSpec.UNSPECIFIED -> context.dpToPx(defValue).toInt()
            else -> MeasureSpec.getSize(spec)
        }
    }

    private fun calculateViewHeight(heightMeasureSpec: Int, textWidth: Int): Int {
        val initSizeHeight = resolveDefaultSize(heightMeasureSpec, DEFAULT_VIEW_SIZE_HEIGHT)
        textHeight = (dataList.size * marginText + getTextViewHeight(textWidth)).toInt()

        val textHeightWithPadding = textHeight + paddingTop + paddingBottom
        return if (textHeightWithPadding > initSizeHeight) textHeightWithPadding else initSizeHeight
    }

    private fun calculateCircleRadius(width: Int, height: Int) {
        val circleViewWidth = (width * CIRCLE_WIDTH_PERCENT)
        circleRadius = if (circleViewWidth > height) {
            (height.toFloat() - circlePadding) / 2
        } else {
            circleViewWidth.toFloat() / 2
        }

        with(circleRect) {
            left = circlePadding
            top = height / 2 - circleRadius
            right = circleRadius * 2 + circlePadding
            bottom = height / 2 + circleRadius
        }

        circleCenterX = (circleRadius * 2 + circlePadding + circlePadding) / 2
        circleCenterY = (height / 2 + circleRadius + (height / 2 - circleRadius)) / 2

        textAmountY = circleCenterY

        val sizeTextAmountNumber = getWidthOfAmountText(
            totalAmount.toString(),
            amountTextPaint
        )

        textAmountXNumber = circleCenterX - sizeTextAmountNumber.width() / 2
        textAmountXDescription = circleCenterX - getWidthOfAmountText(textAmountStr, descriptionTextPain).width() / 2
        textAmountYDescription = circleCenterY + sizeTextAmountNumber.height() + marginTextThird
    }

    private fun getTextViewHeight(maxWidth: Int): Int {
        var textHeight = 0
        dataList.forEach {
            val textLayoutNumber = getMultilineText(
                text = it.amount.toString(),
                textPaint = numberTextPaint,
                width = maxWidth
            )
            val textLayoutDescription = getMultilineText(
                text = it.category,
                textPaint = descriptionTextPain,
                width = maxWidth
            )
            textRowList.apply {
                add(textLayoutNumber)
                add(textLayoutDescription)
            }
            textHeight += textLayoutNumber.height + textLayoutDescription.height
        }

        return textHeight
    }

    private fun calculatePercentageOfData() {
        totalAmount = dataList.fold(0) { res, value -> res + value.amount }

        var startAt = circleSectionSpace
        percentageCircleList = dataList.mapIndexed { index, pair ->
            var percent = pair.amount * 100 / totalAmount.toFloat() - circleSectionSpace
            percent = if (percent < 0F) 0F else percent

            val resultModel = PieChartModel(
                percentOfCircle = percent,
                percentToStartAt = startAt,
                colorOfLine = Color.parseColor(pieChartColors[index % pieChartColors.size]),
                stroke = circleStrokeWidth,
                paintRound = circlePaintRoundSize
            )
            if (percent != 0F) startAt += percent + circleSectionSpace
            resultModel
        }
    }

    private fun getWidthOfAmountText(text: String, textPaint: TextPaint): Rect {
        val bounds = Rect()
        textPaint.getTextBounds(text, 0, text.length, bounds)
        return bounds
    }

    private fun getMultilineText(
        text: CharSequence,
        textPaint: TextPaint,
        width: Int,
        start: Int = 0,
        end: Int = text.length,
        alignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL,
        textDir: TextDirectionHeuristic = TextDirectionHeuristics.LTR,
        spacingMult: Float = 1f,
        spacingAdd: Float = 0f
    ): StaticLayout {

        return StaticLayout.Builder
            .obtain(text, start, end, textPaint, width)
            .setAlignment(alignment)
            .setTextDirection(textDir)
            .setLineSpacing(spacingAdd, spacingMult)
            .build()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            if (event.action == MotionEvent.ACTION_DOWN) {
                val angle = getTouchedAngle(event.x, event.y)
                selectedIndex = getTouchedSectorIndex(angle)
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun getTouchedAngle(x: Float, y: Float): Float {
        val dx = x - circleCenterX
        val dy = y - circleCenterY
        var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
        angle = (angle + 360) % 360
        return angle
    }

    private fun getTouchedSectorIndex(angle: Float): Int {
        percentageCircleList.forEachIndexed { index, pieChartModel ->
            val startAngle = pieChartModel.percentToStartAt
            val endAngle = startAngle + pieChartModel.percentOfCircle

            if (angle in startAngle..endAngle) {
                return index
            }
        }
        return -1
    }
}