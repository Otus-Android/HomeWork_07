package otus.homework.customview.data.graph

import android.graphics.RectF

class ScaleHelper(adapter: ChartAdapter, contentRect: RectF, lineWidth: Float, fill: Boolean) {
  private val width: Float
  private val height: Float
  private val size: Int
  private val xScale: Float
  private val yScale: Float
  private val xTranslation: Float
  private val yTranslation: Float

  init {
    val leftPadding = contentRect.left
    val topPadding = contentRect.top
    var lineWidthOffset = 0f
    if (!fill) lineWidthOffset = lineWidth

    width = contentRect.width() - lineWidthOffset
    height = contentRect.height() - lineWidthOffset

    size = adapter.count
    val bounds = adapter.dataBounds
    bounds.inset(
      (if (bounds.width() == 0f) -1 else 0).toFloat(),
      (if (bounds.height() == 0f) -1 else 0).toFloat()
    )

    val minX = bounds.left
    val maxX = bounds.right
    val minY = bounds.top
    val maxY = bounds.bottom

    xScale = width / (maxX - minX)
    xTranslation = leftPadding - minX * xScale + lineWidthOffset / 2
    yScale = height / (maxY - minY)
    yTranslation = minY * yScale + topPadding + lineWidthOffset / 2
  }

  fun getX(rawX: Float): Float = rawX * xScale + xTranslation

  fun getY(rawY: Float): Float = height - rawY * yScale + yTranslation

  fun getDiffBetweenTwoPoints(x1: Float, x2: Float) = getX(x2) - getX(x1)
}