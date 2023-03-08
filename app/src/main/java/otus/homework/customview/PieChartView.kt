package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

private const val STROKE_WIDTH = 48

class PieChartView @JvmOverloads constructor(
  context: Context,
  attributeSet: AttributeSet? = null,
  defStyleAttrs: Int = 0
) : View(context, attributeSet, defStyleAttrs) {

  var model = PieChartModel(emptyList())
    set(value) {
      field = value
      invalidate()
    }

  private val rect = RectF()

  private val paint = Paint()
    .apply {
      style = Paint.Style.STROKE
      strokeWidth = STROKE_WIDTH.toFloat()
      color = Color.RED
    }

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    val minimalDimension = min(measuredWidth, measuredHeight)
    setMeasuredDimension(minimalDimension, minimalDimension)

    val rectDimension = minimalDimension - STROKE_WIDTH / 2f
    rect.left = STROKE_WIDTH / 2f
    rect.top = STROKE_WIDTH / 2f
    rect.bottom = rectDimension
    rect.right = rectDimension
  }

  private val colors = listOf(
    Color.RED,
    Color.GREEN,
    Color.BLUE,
    Color.MAGENTA
  )

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    var angle = -90f
    for (i in model.items.indices) {
      val sweepAngle = 360 * model.getRatioByIndex(i)
      paint.color = colors[i]
      canvas.drawArc(rect, angle, sweepAngle, false, paint)
      angle += sweepAngle
    }
  }
}
