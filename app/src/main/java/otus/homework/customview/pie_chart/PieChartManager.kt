package otus.homework.customview.pie_chart

import android.graphics.RectF
import android.view.MotionEvent
import otus.homework.customview.ViewUpdateListener
import otus.homework.customview.model.Category
import otus.homework.customview.pie_chart.model.CategoryArc
import otus.homework.customview.pie_chart.model.ChartState
import otus.homework.customview.pie_chart.model.PieConfigViewData
import otus.homework.customview.pie_chart.model.PieChartModel
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

class PieChartManager(private val viewUpdateListener: ViewUpdateListener):
    PieAnimationManager.AnimationListener {

    private var clickListener: (categoryName: String?) -> Unit = {}

    val chartModel = PieChartModel()
    val drawController = PieDrawController(chartModel)
    var currentAnimatedCategoryMax: CategoryArc? = null
    private set

    private val animationManager = PieAnimationManager(chartModel, this)

    private var mainRectF: RectF = RectF(0f, 0f, chartModel.minSizePx.toFloat(), chartModel.minSizePx.toFloat())
    private var rectFIncreased: RectF = RectF(0f, 0f, 0f, 0f)
    private var rectFDecreased: RectF = RectF(0f, 0f, 0f, 0f)

    private var currentAnimatedCategoryMin: CategoryArc? = null

    override fun onIncreaseAnimationUpdate(rectF: RectF) {
        rectFDecreased = rectF
        drawController.updateRectDecreased(rectFDecreased)
        viewUpdateListener.onRequestInvalidate()
    }

    override fun onDecreaseAnimationUpdate(rectF: RectF) {
        rectFIncreased = rectF
        drawController.updateRectIncreased(rectFIncreased)
        viewUpdateListener.onRequestInvalidate()
    }

    fun config(configViewData: PieConfigViewData) {
        chartModel.config(
            configViewData.minSizePx,
            configViewData.innerSizePercent,
            configViewData.animatedSizePercent
        )
        setPayload(configViewData.payload)
        clickListener = configViewData.clickListener
    }

    fun onLayout(
        paddingLeft: Int,
        paddingRight: Int,
        paddingTop: Int,
        paddingBottom: Int,
        measuredHeight: Int,
        measuredWidth: Int
    ) {
        rectFIncreased.apply {
            this.left = paddingLeft.toFloat()
            this.top = paddingTop.toFloat()
            this.right = measuredWidth - paddingRight.toFloat()
            this.bottom = measuredHeight - paddingBottom.toFloat()
        }
        drawController.updateRectIncreased(rectFIncreased)
        val defaultRadius = (rectFIncreased.right - rectFIncreased.left) / 2 / chartModel.animatedSizePercent
        chartModel.animateTo = ((rectFIncreased.right - rectFIncreased.left) / 2) - defaultRadius

        if (currentAnimatedCategoryMax != null) {
            rectFDecreased.apply {
                this.left = paddingLeft.toFloat()
                this.top = paddingTop.toFloat()
                this.right = measuredWidth - paddingRight.toFloat()
                this.bottom = measuredHeight - paddingBottom.toFloat()
            }
        } else {
            rectFDecreased.apply {
                this.left = chartModel.animateTo
                this.top = chartModel.animateTo
                this.right = measuredWidth - paddingRight.toFloat() - chartModel.animateTo
                this.bottom = measuredHeight - paddingBottom.toFloat() - chartModel.animateTo
            }
        }
        drawController.updateRectDecreased(rectFDecreased)

        mainRectF.let {
            it.left = chartModel.animateTo
            it.top = chartModel.animateTo
            it.right = measuredWidth - paddingRight.toFloat() - chartModel.animateTo
            it.bottom = measuredHeight - paddingBottom.toFloat() - chartModel.animateTo
        }
        drawController.updateMainRect(mainRectF)
        chartModel.outerRadius = (mainRectF.right - mainRectF.left) / 2
        chartModel.innerRadius = chartModel.outerRadius * chartModel.innerSizePercent
    }

    fun onTouch(event: MotionEvent): Boolean {
        if (!mainRectF.contains(event.x, event.y)) return false

        if (event.action == MotionEvent.ACTION_DOWN) {
            val angle = findTouchAngle(event)
            if (angle != null)
                onClick(angle.toFloat())
            else return false
        }

        return true
    }

    fun updateData(payload: ChartState?) {
        chartModel.drawData = payload?.listCategory ?: emptyList()
        currentAnimatedCategoryMax = payload?.selectedCategory

        viewUpdateListener.onRequestInvalidate()
    }

    private fun findTouchAngle(event: MotionEvent): Double? {
        val touchX = event.x - mainRectF.left
        val touchY = event.y - mainRectF.top

        val centerX = mainRectF.centerX() - mainRectF.left
        val centerY = mainRectF.centerY() - mainRectF.top

        val distanceToCenter = sqrt(
            abs(touchX - centerX).toDouble().pow(2.0) + abs(touchY - centerY).toDouble().pow(2.0)
        )
        if (distanceToCenter > chartModel.outerRadius || distanceToCenter < chartModel.innerRadius) return null

        val radius = (mainRectF.right - mainRectF.left) / 2

        val angle = Math.toDegrees(
            atan2(
                ((touchY - radius)).toDouble(),
                ((touchX - radius)).toDouble()
            ) + Math.PI / 2
        )
        return if (angle < 0) 360 - abs(angle) else angle
    }

    private fun onClick(angle: Float) {
        chartModel.drawData.find {
            it.startAngle < angle && it.startAngle + it.sweepAngle > angle
        }?.let {
            currentAnimatedCategoryMin = if (currentAnimatedCategoryMax != null)
                currentAnimatedCategoryMax
            else
                null

            currentAnimatedCategoryMax = if (currentAnimatedCategoryMin == it)
                null
            else
                it

            clickListener.invoke(if (currentAnimatedCategoryMax != null) it.categoryName else null)
        }
        animateOnClick()
    }

    private fun animateOnClick() {
        drawController.updateCategoryMax(currentAnimatedCategoryMax)
        drawController.updateCategoryMin(currentAnimatedCategoryMin)

        //Увеличиваем
        currentAnimatedCategoryMax?.let {
            animationManager.animationIncrease(mainRectF)
        }

        //Уменьшаем
        currentAnimatedCategoryMin?.let {
            animationManager.animationDecrease(mainRectF)
        }
    }

    private fun setPayload(payload: List<Category>) {
        chartModel.inputData = payload
        var startAngle = 0f
        val data = payload.toList().sortedByDescending { it.totalAmount }
        val maxSum = data.sumBy { it.totalAmount }
        updateData(
            ChartState(
                data.map { category ->
                    val sweepAngle = (category.totalAmount.toFloat() / maxSum.toFloat()) * 360f
                    val arc = CategoryArc(
                        categoryName = category.name,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        color = category.color
                    )
                    startAngle += sweepAngle
                    return@map arc
                },
                null
            )
        )
    }
}