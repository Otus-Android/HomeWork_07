package otus.homework.customview.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

// lines
private const val LINE_WIDTH = 8f

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

class DynamicChartView @JvmOverloads constructor(
  context: Context,
  attributeSet: AttributeSet? = null,
  defStyleAttrs: Int = 0
) : View(context, attributeSet, defStyleAttrs) {

  private var model = DynamicChartModel(emptyList())

  private val paint = Paint()
    .apply {
      style = Paint.Style.STROKE
      strokeWidth = LINE_WIDTH
    }

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec)
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    model.lines.forEachIndexed { index, line ->
      paint.color = COLORS[index % COLORS.size]
      canvas.drawLine(0f, 100f * index, measuredWidth.toFloat(), measuredHeight.toFloat() - 100f * index, paint)
    }
  }

  fun updateData(model: DynamicChartModel) {
    this.model = model
    invalidate()
  }
}
