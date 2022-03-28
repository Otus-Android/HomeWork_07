package otus.homework.customview.pie_chart

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import otus.homework.customview.pie_chart.model.CategoryArc
import otus.homework.customview.pie_chart.model.PieChartModel

class PieDrawController(private val chartModel: PieChartModel) {

    private val paint = Paint().apply {
        isAntiAlias = true
    }
    private val emptyPaint = Paint().apply {
        isAntiAlias = true
        xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
    }

    private var mainRectF: RectF = RectF(0f, 0f, 0f, 0f)
    private var rectFIncreased: RectF = RectF(0f, 0f, 0f, 0f)
    private var rectFDecreased: RectF = RectF(0f, 0f, 0f, 0f)
    private var currentAnimatedCategoryMax: CategoryArc? = null
    private var currentAnimatedCategoryMin: CategoryArc? = null

    fun updateMainRect(mainRectF: RectF) {
        this.mainRectF = mainRectF
    }

    fun updateRectIncreased(rectFIncreased: RectF) {
        this.rectFIncreased = rectFIncreased
    }

    fun updateRectDecreased(rectFDecreased: RectF) {
        this.rectFDecreased = rectFDecreased
    }

    fun updateCategoryMax(categoryMax: CategoryArc?) {
        this.currentAnimatedCategoryMax = categoryMax
    }

    fun updateCategoryMin(categoryMin: CategoryArc?) {
        this.currentAnimatedCategoryMin = categoryMin
    }

    fun draw(canvas: Canvas) {
        canvas.save()
        canvas.rotate(270f, mainRectF.centerX(), mainRectF.centerY())
        chartModel.drawData.forEach { category ->
            paint.color = category.color
            if (category == currentAnimatedCategoryMax) {
                canvas.drawArc(
                    rectFDecreased,
                    category.startAngle,
                    category.sweepAngle,
                    true,
                    paint
                )
            }

            if (category == currentAnimatedCategoryMin) {
                canvas.drawArc(
                    rectFIncreased,
                    category.startAngle,
                    category.sweepAngle,
                    true,
                    paint
                )
            }
            canvas.drawArc(mainRectF, category.startAngle, category.sweepAngle, true, paint)
        }
        canvas.drawCircle(mainRectF.centerX(), mainRectF.centerY(), chartModel.innerRadius, emptyPaint)
        canvas.restore()
    }

}