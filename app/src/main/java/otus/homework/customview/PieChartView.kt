package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

private const val STROKE_WIDTH = 48f

class PieChartView @JvmOverloads constructor(
  context: Context,
  attributeSet: AttributeSet? = null,
  defStyleAttrs: Int = 0
) : View(context, attributeSet, defStyleAttrs) {

  private val rect = RectF()

  private val paint = Paint()
    .apply {
      style = Paint.Style.STROKE
      strokeWidth = STROKE_WIDTH
      color = Color.RED
    }

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    val minimalDimension = min(measuredWidth, measuredHeight)
    setMeasuredDimension(minimalDimension, minimalDimension)

    val rectDimension = minimalDimension - STROKE_WIDTH / 2
    rect.bottom = rectDimension
    rect.right = rectDimension
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    canvas.drawArc(rect, 0f, 90f, false, paint)
  }
}
