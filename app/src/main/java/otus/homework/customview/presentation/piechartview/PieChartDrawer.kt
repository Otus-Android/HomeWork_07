package otus.homework.customview.presentation.piechartview

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.View
import otus.homework.customview.presentation.model.PieChartAngle

class PieChartDrawer(
    private val pieChartContainer: RectF,
    private val sectorPaint: Paint,
    private val selectionPaint: Paint
) {

    fun setPieChartContainerRectBounds(view : View, pieChartPaddingPx: Int) {
        val centerX = view.width / 2
        val centerY = view.height / 2
        val pieChartSizeWithPadding = view.measuredWidth - pieChartPaddingPx
        pieChartContainer.left = (centerX - pieChartSizeWithPadding / 2).toFloat()
        pieChartContainer.top = (centerY - pieChartSizeWithPadding / 2).toFloat()
        pieChartContainer.right = (centerX + pieChartSizeWithPadding / 2).toFloat()
        pieChartContainer.bottom = (centerY + pieChartSizeWithPadding / 2).toFloat()
    }

    fun drawPieChart(canvas: Canvas, angles: List<PieChartAngle>) {
        for (angle in angles) {
            drawSector(canvas, angle)
        }
    }

    private fun drawSector(canvas: Canvas, angle: PieChartAngle) {
        sectorPaint.color = angle.color.intColor
        canvas.drawArc(pieChartContainer, angle.startAngle, angle.sweepAngle, true, sectorPaint)
    }

    fun drawSelectedSector(canvas: Canvas, selectedSector: PieChartAngle?) {
        selectedSector?.let {
            canvas.drawArc(pieChartContainer, it.startAngle, it.sweepAngle, true, selectionPaint)
        }
    }
}