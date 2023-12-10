package otus.homework.customview.presentation.pie.chart.cursor

import android.graphics.PointF
import otus.homework.customview.presentation.pie.chart.area.PieAreaProvider
import otus.homework.customview.presentation.pie.chart.models.PieAreaNode
import otus.homework.customview.presentation.pie.chart.models.PieDataProvider
import otus.homework.customview.presentation.pie.chart.utils.MathUtils

class CursorStorage(
    private val areaProvider: PieAreaProvider,
    private val dataProvider: PieDataProvider
) {

    private var cursorPosition: CursorPosition? = null

    fun update(x: Float, y: Float): Boolean {
        val localArea = areaProvider.chart
        val radius = localArea.width() / 2f
        val xO = localArea.centerX() - x
        val yO = localArea.centerY() - y
        return if (xO * xO + yO * yO < radius * radius) {
            val angle = MathUtils.calculateTheta(
                centerX = areaProvider.default.centerX(),
                centerY = areaProvider.default.centerY(),
                x = x,
                y = y
            )
            cursorPosition = CursorPosition(
                PointF(x, y),
                angle,
                dataProvider.getNode(angle)
            )
            true
        } else {
            false
        }
    }

    fun getNode() = cursorPosition?.node

    fun clear() {
        cursorPosition = null
    }

    private data class CursorPosition(
        val pointF: PointF,
        val angle: Float,
        val node: PieAreaNode?
    )
}