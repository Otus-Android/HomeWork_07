package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

private const val STROKE_WIDTH = 100
private const val GAP_ANGLE = 1.5f

private val COLORS = listOf(
  Color.parseColor("#ff0000"),
  Color.parseColor("#ff8700"),
  Color.parseColor("#ffd300"),
  Color.parseColor("#deff0a"),
  Color.parseColor("#a1ff0a"),
  Color.parseColor("#0aff99"),
  Color.parseColor("#0aefff"),
  Color.parseColor("#147df5"),
  Color.parseColor("#580aff"),
  Color.parseColor("#be0aff")
)

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

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    var angle = -90f
    for (i in model.items.indices) {
      val sweepAngle = 360 * model.getRatioByIndex(i)
      paint.color = COLORS[i % COLORS.size]
      canvas.drawArc(rect, angle, sweepAngle - GAP_ANGLE, false, paint)
      angle += sweepAngle
    }
  }
}
