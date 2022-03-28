package otus.homework.customview.detail_chart

import android.content.Context
import android.graphics.Canvas
import android.graphics.PathEffect
import android.graphics.RectF
import otus.homework.customview.ViewUpdateListener
import otus.homework.customview.asPixel
import otus.homework.customview.detail_chart.model.DatePurchase
import otus.homework.customview.detail_chart.model.DetailChartModel
import otus.homework.customview.detail_chart.model.LineText
import otus.homework.customview.detail_chart.model.Point
import otus.homework.customview.formatToString
import otus.homework.customview.model.Purchase
import otus.homework.customview.roundFormat
import otus.homework.customview.toMoneyFormat
import otus.homework.customview.utils.DateComparator
import java.util.*
import kotlin.math.pow

class DetailChartManager(
    context: Context,
    private val viewUpdateListener: ViewUpdateListener
) : DetailAnimationManager.AnimationListener {

    private val drawController = DetailDrawController(context)
    private val animationManager = DetailAnimationManager(this)

    private val defaultSizeHeight = context.asPixel(100)
    private val defaultSizeWidth = context.asPixel(200)

    var purchases: List<DatePurchase> = emptyList()
        private set

    private var maxLengthAmountString: String = ""
    private var divider: Double = 0.0

    private var detailChartModel = DetailChartModel(
        mainRectF = RectF(0f, 0f, defaultSizeWidth.toFloat(), defaultSizeHeight.toFloat()),
        chartTextPadding = context.asPixel(4),
    )

    private val dateComparator = DateComparator()

    override fun onAnimateChart(pathEffect: PathEffect) {
        drawController.paintChart.pathEffect = pathEffect
        viewUpdateListener.onRequestInvalidate()
    }

    override fun onAnimateDots(alpha: Int) {
        drawController.paintCircle.alpha = alpha
        viewUpdateListener.onRequestInvalidate()
    }

    fun setPayload(payload: List<Purchase>) {
        val purchasesPerDay = mutableListOf<DatePurchase>()
        payload.map { purchase ->
            val calendar = GregorianCalendar.getInstance().apply {
                timeInMillis = purchase.time.toLong() * 1000
            }
            val datePurchase = DatePurchase(
                purchase.amount,
                calendar,
                calendar.time.formatToString()
            )
            val availableDay =
                purchasesPerDay.find { dateComparator.compare(it.time, calendar) == 0 }
            if (availableDay != null) {
                availableDay.amount = availableDay.amount + purchase.amount
            } else {
                purchasesPerDay.add(datePurchase)
            }
            return@map datePurchase
        }.sortedBy {
            it.time.timeInMillis
        }
        updateData(purchasesPerDay)
    }

    fun draw(canvas: Canvas) {
        drawController.draw(canvas, detailChartModel)
    }

    fun onMeasure() {
        drawController.painText.getTextBounds(
            maxLengthAmountString,
            0,
            maxLengthAmountString.length,
            detailChartModel.boundsHorizontal
        )
        val dateText = purchases[0].stringTime
        drawController.painText.getTextBounds(
            dateText,
            0,
            dateText.length,
            detailChartModel.boundsVertical
        )
    }

    fun onLayout(
        paddingLeft: Int,
        paddingRight: Int,
        paddingTop: Int,
        paddingBottom: Int,
        measuredHeight: Int,
        measuredWidth: Int
    ) {
        if (purchases.isEmpty()) return
        detailChartModel = detailChartModel.copy(
            mainRectF = calculateMainField(
                paddingLeft,
                paddingRight,
                paddingTop,
                paddingBottom,
                measuredHeight,
                measuredWidth
            )
        )
        val horizontalLinesCount = getHorizontalLinesCount()
        detailChartModel = detailChartModel.copy(
            horizontalLines = getHorizontalLinesPoints(horizontalLinesCount)
        )
        val verticalMaxLinesCount =
            (detailChartModel.mainRectF.width() / detailChartModel.boundsVertical.width()).toInt()
        detailChartModel = detailChartModel.copy(
            verticalLines = getVerticalLinesPoints(verticalMaxLinesCount, purchases)
        )
        detailChartModel = detailChartModel.copy(
            chartPoints = getChartPoints(detailChartModel.verticalLines)
        )
        showChart()
    }

    private fun calculateMainField(
        paddingLeft: Int,
        paddingRight: Int,
        paddingTop: Int,
        paddingBottom: Int,
        measuredHeight: Int,
        measuredWidth: Int
    ): RectF {
        val fieldRight = measuredWidth -
            paddingRight.toFloat() -
            detailChartModel.boundsHorizontal.width() -
            (detailChartModel.chartTextPadding * 2) -
            (drawController.paintFrame.strokeWidth / 2)
        val fieldBottom = measuredHeight -
            paddingBottom.toFloat() -
            detailChartModel.boundsVertical.height() -
            (drawController.paintFrame.strokeWidth / 2)
        return detailChartModel.mainRectF.apply {
            left = paddingLeft + drawController.paintFrame.strokeWidth
            top = paddingTop + drawController.paintFrame.strokeWidth
            right = fieldRight
            bottom = fieldBottom
        }
    }

    fun updateData(data: List<DatePurchase>) {
        purchases = data
        val maxAmount = purchases.maxOf { it.amount }
        detailChartModel = detailChartModel.copy(
            maxRoundedAmount = getMaxChartValue(maxAmount)
        )
        maxLengthAmountString = detailChartModel.maxRoundedAmount.toMoneyFormat()
        viewUpdateListener.onRequestLayout()
        viewUpdateListener.onRequestInvalidate()
    }

    private fun showChart() {
        calculateChart()
        animationManager.startAnimate(detailChartModel.chartPath)
    }

    private fun calculateChart() {
        detailChartModel.chartPath.reset()
        val firstPoint = detailChartModel.chartPoints[0]
        detailChartModel.chartPath.moveTo(firstPoint.x, firstPoint.y)
        detailChartModel.chartPoints.forEachIndexed { index, point ->
            if (index == 0) {
                detailChartModel.chartPath.lineTo(point.x, point.y)
                return@forEachIndexed
            }
            val prevX = detailChartModel.chartPoints[index - 1].x
            val prevY = detailChartModel.chartPoints[index - 1].y
            detailChartModel.chartPath.cubicTo(
                point.x - (point.x - prevX) / 2,
                prevY,
                prevX + (point.x - prevX) / 2,
                point.y,
                point.x,
                point.y
            )
        }
        drawController.paintCircle.alpha = 0
    }

    private fun getMaxChartValue(number: Int): Int {
        val divider = 10.0.pow((number.toString().length - 1).toDouble())
        val value = (number / divider).roundFormat().toInt() * divider
        this.divider = divider
        return if ((value - (divider / 2) < number)) value.toInt() else (value - (divider / 2)).toInt()
    }

    private fun getHorizontalLinesCount(): Int {
        val maxVerticalLines =
            (detailChartModel.mainRectF.height() / detailChartModel.boundsHorizontal.height() / 3).toInt()
        (maxVerticalLines downTo 0).forEach { // Find round step
            val step = detailChartModel.maxRoundedAmount / it
            if (step != 0 && detailChartModel.maxRoundedAmount % step == 0) return it
        }
        return 2
    }

    private fun getChartPoints(verticalPoints: List<LineText>): List<Point> {
        val listToReturn = mutableListOf<Point>()
        verticalPoints.forEach { lineText ->
            val amount = purchases.find {
                it.stringTime == lineText.text
            }?.amount ?: 0
            val x = lineText.startX
            val y =
                detailChartModel.mainRectF.bottom - (amount * detailChartModel.mainRectF.height() / detailChartModel.maxRoundedAmount)
            listToReturn.add(Point(x, y))
        }
        return listToReturn
    }

    private fun getVerticalLinesPoints(
        horizontalMaxLinesCount: Int,
        purchaseData: List<DatePurchase>
    ): List<LineText> {
        val data = purchaseData.toMutableList()
        if (data.size < horizontalMaxLinesCount) {
            val step =
                if (data.size != 1) detailChartModel.mainRectF.width() / (data.size - 1) else 1.toFloat()
            return data.mapIndexed { index, datePurchase ->
                var x = if (data.size != 1) step * index else detailChartModel.mainRectF.width() / 2
                x += detailChartModel.mainRectF.left
                LineText(
                    startX = x,
                    startY = detailChartModel.mainRectF.bottom,
                    stopX = x,
                    stopY = detailChartModel.mainRectF.top,
                    datePurchase.stringTime
                )
            }
        } else {
            val newList = mutableListOf<DatePurchase>()
            data.forEachIndexed { index, datePurchase ->
                if ((index + 1) % 2 != 0 || index == data.size)
                    newList.add(datePurchase)
            }
            return getVerticalLinesPoints(horizontalMaxLinesCount, newList)
        }
    }

    private fun getHorizontalLinesPoints(horizontalLinesCount: Int): List<LineText> {
        val stepY = detailChartModel.mainRectF.height() / horizontalLinesCount
        val stepAmount = detailChartModel.maxRoundedAmount / horizontalLinesCount
        return (1 until horizontalLinesCount).map {
            LineText(
                startX = detailChartModel.mainRectF.left,
                startY = stepY * it + detailChartModel.mainRectF.top,
                stopX = detailChartModel.mainRectF.right,
                stopY = stepY * it + detailChartModel.mainRectF.top,
                text = (detailChartModel.maxRoundedAmount - (it * stepAmount)).toMoneyFormat()
            )
        }.toList()
    }
}