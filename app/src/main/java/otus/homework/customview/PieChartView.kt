package otus.homework.customview

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.Parcelable
import android.text.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

private const val TAG = "PieChartView"

class PieChartView(context: Context, attributeSet: AttributeSet):
    View(context, attributeSet) {

    companion object {
        private const val DEFAULT_MARGIN_TEXT_1 = 2
        private const val DEFAULT_MARGIN_TEXT_2 = 10
        private const val DEFAULT_MARGIN_TEXT_3 = 2
        private const val DEFAULT_MARGIN_SMALL_CIRCLE = 12
        private const val TEXT_WIDTH_PERCENT = 0.40
        private const val CIRCLE_WIDTH_PERCENT = 0.50
        private const val DEFAULT_VIEW_SIZE_HEIGHT = 150
        private const val DEFAULT_VIEW_SIZE_WIDTH = 250
    }

    private var marginTextFirst: Float = context.dpToPixels(DEFAULT_MARGIN_TEXT_1)
    private var marginTextSecond: Float = context.dpToPixels(DEFAULT_MARGIN_TEXT_2)
    private var marginTextThird: Float = context.dpToPixels(DEFAULT_MARGIN_TEXT_3)
    private var marginSmallCircle: Float = context.dpToPixels(DEFAULT_MARGIN_SMALL_CIRCLE)
    private val marginText: Float = marginTextFirst + marginTextSecond
    private val circleRect = RectF()
    private var circleStrokeWidth: Float = context.dpToPixels(5)
    private var circleRadius: Float = 0F
    private var circlePadding: Float = context.dpToPixels(5)
    private var circleSectionSpace: Float = 0F
    private var circleCenterX: Float = 0F
    private var circleCenterY: Float = 0F
    private var numberTextPaint: TextPaint = TextPaint()
    private var descriptionTextPain: TextPaint = TextPaint()
    private var amountTextPaint: TextPaint = TextPaint()
    private var textStartX: Float = 0F
    private var textStartY: Float = 0F
    private var textHeight: Int = 0
    private var textCircleRadius: Float = context.dpToPixels(4)
    private var textAmountStr: String = ""
    private var textAmountY: Float = 0F
    private var textAmountXNumber: Float = 0F
    private var textAmountXDescription: Float = 0F
    private var textAmountYDescription: Float = 0F
    private var totalAmount: Int = 0
    private var pieChartColors: List<String> = listOf()
    private var percentageCircleList: List<PieChartModel> = listOf()
    private var textRowList: MutableList<StaticLayout> = mutableListOf()
    private var dataList: List<PayLoadModel> = listOf()
    private var animationSweepAngle: Int = 0
    private val touchMargin: Float = 10F    // отступ от окружности при касании,
                                            // на которой всё еще будет срабатывать тачивент

    interface Callbacks {
        fun onSectorSelected(valueModel: BaseValueModel)
    }

    private var callbacks: Callbacks? = null

    init {
        // Задаем базовые значения и конвертируем в px
        val textAmountSize: Float = context.spToPixels(10)
        val textNumberSize: Float = context.spToPixels(5)
        val textDescriptionSize: Float = context.spToPixels(14)
        val textAmountColor: Int = Color.WHITE
        val textNumberColor: Int = Color.WHITE
        val textDescriptionColor: Int = Color.GRAY

        val typeArray = context.obtainStyledAttributes(attributeSet, R.styleable.PieChartView)

        // Секция списка цветов
        val colorResId =
            typeArray.getResourceId(R.styleable.PieChartView_pieChartColors, 0)
        pieChartColors = typeArray.resources.getStringArray(colorResId).toList()

        circlePadding += circleStrokeWidth

        initPains(amountTextPaint, textAmountSize, textAmountColor)
        initPains(numberTextPaint, textNumberSize, textNumberColor)
        initPains(descriptionTextPain, textDescriptionSize, textDescriptionColor, true)

        typeArray.recycle()

    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        callbacks = context as Callbacks?
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        callbacks = null
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

    override fun onRestoreInstanceState(state: Parcelable?) {
        val baseChartState = state as? BaseChartState
        super.onRestoreInstanceState(baseChartState?.superState ?: state)

        dataList = baseChartState?.dataList ?: listOf()
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        return BaseChartState(superState, dataList)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // обработка нажатия пальца на экран
                val segmentLength = sqrt(
                    (event.x - this.circleCenterX).toDouble().pow(2.0) +
                            (event.y - this.circleCenterY).toDouble().pow(2.0)
                )
                if (circleRect.contains(event.x, event.y)
                    && segmentLength <= this.circleRadius + touchMargin) {

                    // прямоугольник (квадрат), в который вписана окружность, содержит координаты клика
                    // и длина отрезка от центра окружности до точки клика меньше или равна
                    // радиусу окружности + разрешенный промах touchMargin

                    //Log.d(TAG, "${this.circleRect.contains(event.x, event.y)}")
                    //Log.d(TAG, "r=$r, circleRadius = ${this.circleRadius}, r<radius=${r<this.circleRadius}")
                    val angle = angleBetween2Lines(
                        PointF(circleCenterX, circleCenterY),
                        PointF(circleCenterX + circleRadius, circleCenterY),
                        PointF(circleCenterX, circleCenterY),
                        PointF(event.x, event.y)
                    )
                    //Log.d(TAG, "angle = $angle")
                    val touchedSector =
                        percentageCircleList.find { a ->
                            angle in a.percentToStartAt..a.absPercentOfCircle }
                    Log.d(TAG,
                        "${touchedSector?.percentToStartAt} to ${touchedSector?.absPercentOfCircle}")
                    if (touchedSector != null) {
                        callbacks?.onSectorSelected(touchedSector.valueModel)
                    }
                }
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                // обработка перемещения пальца по экрану
                return true
            }
            MotionEvent.ACTION_UP -> {
                // обработка отпускания пальца от экрана
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas) {

        super.onDraw(canvas)

        drawCircle(canvas)
        drawText(canvas)

    }

    private fun initPains(textPaint: TextPaint, textSize: Float, textColor: Int, isDescription: Boolean = false) {
        textPaint.color = textColor
        textPaint.textSize = textSize
        textPaint.isAntiAlias = true

        if (!isDescription) textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    fun setValues(list: List<PayLoadModel>) {
        dataList = list
        calculatePercentageOfData()
    }

    private fun calculatePercentageOfData() {

        totalAmount = dataList.sumOf { payLoadItem -> payLoadItem.amount }

        var startAt = circleSectionSpace
        percentageCircleList = dataList.mapIndexed { index, payLoad ->
            var percent = payLoad.amount * 100 / totalAmount.toFloat()
            percent = if (percent < 0F) 0F else percent

            val resultModel = PieChartModel(
                percentToStartAt = startAt,
                percentOfCircle = percent,
                absPercentOfCircle = startAt + percent,
                colorOfLine = Color.parseColor(pieChartColors[index % pieChartColors.size]),
                valueModel = payLoad
            )
            if (percent != 0F) startAt += percent + circleSectionSpace
            resultModel
        }
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
        spacingAdd: Float = 0f) : StaticLayout {

        return StaticLayout.Builder
            .obtain(text, start, end, textPaint, width)
            .setAlignment(alignment)
            .setTextDirection(textDir)
            .setLineSpacing(spacingAdd, spacingMult)
            .build()
    }

    private fun drawCircle(canvas: Canvas) {

        for(percent in percentageCircleList) {

            if (animationSweepAngle > percent.percentToStartAt + percent.percentOfCircle) {
                canvas.drawArc(
                    circleRect,
                    percent.percentToStartAt,
                    percent.percentOfCircle,
                    true,
                    percent.paint)
            } else if (animationSweepAngle > percent.percentToStartAt) {
                canvas.drawArc(
                    circleRect,
                    percent.percentToStartAt,
                    animationSweepAngle - percent.percentToStartAt,
                    true,
                    percent.paint)
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
                // Прибавим высоту и отступ к Y
                textBuffY += staticLayout.height + marginTextFirst
            } else {
                // Описание значения
                staticLayout.draw(canvas, textStartX, textBuffY)
                textBuffY += staticLayout.height + marginTextSecond
            }
        }

    }

    fun startAnimation() {
        val animator = ValueAnimator.ofInt(0, 360).apply {
            duration = 1000 // длительность 1.0 секунда
            interpolator = FastOutSlowInInterpolator()
            addUpdateListener { valueAnimator ->
                animationSweepAngle = valueAnimator.animatedValue as Int
                invalidate()
            }
        }
        animator.start()
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

        textAmountXNumber = circleCenterX -  sizeTextAmountNumber.width() / 2
        textAmountXDescription = circleCenterX - getWidthOfAmountText(textAmountStr, descriptionTextPain).width() / 2
        textAmountYDescription = circleCenterY + sizeTextAmountNumber.height() + marginTextThird
    }

    private fun getWidthOfAmountText(text: String, textPaint: TextPaint): Rect {
        val bounds = Rect()
        textPaint.getTextBounds(text, 0, text.length, bounds)
        return bounds
    }

    private fun calculateViewHeight(heightMeasureSpec: Int, textWidth: Int): Int {
        val initSizeHeight = resolveDefaultSize(heightMeasureSpec, DEFAULT_VIEW_SIZE_HEIGHT)
        textHeight = (dataList.size * marginText + getTextViewHeight(textWidth)).toInt()

        val textHeightWithPadding = textHeight + paddingTop + paddingBottom
        return if (textHeightWithPadding > initSizeHeight) textHeightWithPadding else initSizeHeight
    }

    private fun resolveDefaultSize(spec: Int, defValue: Int): Int {
        return when(MeasureSpec.getMode(spec)) {
            MeasureSpec.UNSPECIFIED -> context.dpToPixels(defValue).toInt()
            else -> MeasureSpec.getSize(spec)
        }
    }

    private fun getTextViewHeight(maxWidth: Int): Int {
        var legendHeight = 0
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
            legendHeight += textLayoutNumber.height + textLayoutDescription.height
        }

        return legendHeight
    }

    private fun angleBetween2Lines(A1: PointF, A2: PointF, B1: PointF, B2: PointF): Float {
        val angle1 = atan2((A2.y - A1.y).toDouble(), (A1.x - A2.x).toDouble()).toFloat()
        val angle2 = atan2((B2.y - B1.y).toDouble(), (B1.x - B2.x).toDouble()).toFloat()
        var calculatedAngle = Math.toDegrees((angle1 - angle2).toDouble()).toFloat()
        if (calculatedAngle < 0) calculatedAngle += 360f
        return calculatedAngle
    }

}