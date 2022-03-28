package otus.homework.customview.detail_chart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import otus.homework.customview.asPixel
import otus.homework.customview.detail_chart.model.DetailChartModel
import otus.homework.customview.toMoneyFormat

class DetailDrawController(
    private val context: Context
) {

    val painText = Paint().apply {
        textSize = context.asPixel(14).toFloat()
        isAntiAlias = true
        color = Color.BLACK
    }
    val paintFrame = Paint().apply {
        isAntiAlias = true
        color = Color.GRAY
        style = Paint.Style.STROKE
        strokeWidth = context.asPixel(1).toFloat()
        pathEffect = DashPathEffect(
            floatArrayOf(context.asPixel(4).toFloat(), context.asPixel(4).toFloat()),
            0f
        )
        alpha = 50
    }
    val paintChart = Paint().apply {
        isAntiAlias = true
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = context.asPixel(3).toFloat()
    }
    val paintCircle = Paint().apply {
        isAntiAlias = true
        color = Color.RED
        style = Paint.Style.FILL
        strokeWidth = context.asPixel(1).toFloat()
    }

    fun draw(canvas: Canvas, detailChartModel: DetailChartModel) {
        drawBoundaries(canvas, detailChartModel)
        drawHorizontalLines(canvas, detailChartModel)
        drawVerticalLines(canvas, detailChartModel)
        drawChart(canvas, detailChartModel)
    }

    private fun drawChart(canvas: Canvas, detailChartModel: DetailChartModel) {
        detailChartModel.chartPoints.forEachIndexed { index, point ->
            if (index == 0 || index == detailChartModel.chartPoints.size - 1) return@forEachIndexed
            canvas.drawCircle(point.x, point.y, 15f, paintCircle)
        }
        canvas.drawPath(detailChartModel.chartPath, paintChart)
    }

    private fun drawHorizontalLines(canvas: Canvas, detailChartModel: DetailChartModel) {
        detailChartModel.horizontalLines.forEach {
            canvas.drawLine(it.startX, it.startY, it.stopX, it.stopY, paintFrame)
            canvas.drawText(
                it.text,
                it.stopX + detailChartModel.chartTextPadding,
                it.stopY + detailChartModel.boundsHorizontal.height(),
                painText
            )
        }
    }

    private fun drawVerticalLines(canvas: Canvas, detailChartModel: DetailChartModel) {
        detailChartModel.verticalLines.forEachIndexed { index, lineText ->
            val x = when {
                (index == 0 && detailChartModel.verticalLines.size > 1) -> lineText.startX
                (index == detailChartModel.verticalLines.size - 1 && detailChartModel.verticalLines.size > 1) -> lineText.startX - detailChartModel.boundsVertical.width()
                else -> lineText.startX - (detailChartModel.boundsVertical.width() / 2)
            }
            canvas.drawText(
                lineText.text,
                x,
                lineText.startY + detailChartModel.boundsVertical.height() + detailChartModel.chartTextPadding,
                painText
            )
            if (detailChartModel.verticalLines.size  == 1 || index != 0 && index != detailChartModel.verticalLines.size - 1)
                canvas.drawLine(lineText.startX, lineText.startY, lineText.stopX, lineText.stopY, paintFrame)
        }
    }

    private fun drawBoundaries(canvas: Canvas, detailChartModel: DetailChartModel) {
        canvas.drawRect(detailChartModel.mainRectF, paintFrame)
        canvas.drawText(
            detailChartModel.maxRoundedAmount.toMoneyFormat(),
            detailChartModel.mainRectF.right + detailChartModel.chartTextPadding,
            detailChartModel.mainRectF.top + detailChartModel.boundsHorizontal.height(),
            painText
        )
    }

}