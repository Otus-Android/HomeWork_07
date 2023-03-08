package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

// strokes
private const val STROKE_WIDTH = 100f
private const val ACCENT_RATIO = 1.8f
private const val ACCENT_STROKE_WIDTH = ACCENT_RATIO * STROKE_WIDTH

// sections
private const val GAP_ANGLE = 1.5f
private const val NONE_SECTION_INDEX = -1

// text
private const val TEXT_SIZE = 96f

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

  private var accentSectionIndex: Int = NONE_SECTION_INDEX

  private val rect = RectF()

  private val paint = Paint()
    .apply {
      style = Paint.Style.STROKE
      strokeWidth = STROKE_WIDTH
    }

  private val textPaint = Paint()
    .apply {
      color = Color.BLACK
      textSize = TEXT_SIZE
    }

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    val minimalDimension = min(measuredWidth, measuredHeight)
    setMeasuredDimension(minimalDimension, minimalDimension)

    val rectDimension = minimalDimension - ACCENT_STROKE_WIDTH / 2f
    rect.left = ACCENT_STROKE_WIDTH / 2f
    rect.top = ACCENT_STROKE_WIDTH / 2f
    rect.bottom = rectDimension
    rect.right = rectDimension
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    drawSections(canvas)
    drawText(canvas)
  }

  private fun drawSections(canvas: Canvas) {
    var angle = -90f
    for (i in model.items.indices) {
      val sweepAngle = 360 * model.getRatioByIndex(i)
      paint.color = COLORS[i % COLORS.size]
      paint.strokeWidth = if (i == accentSectionIndex) ACCENT_STROKE_WIDTH else STROKE_WIDTH
      canvas.drawArc(rect, angle, sweepAngle - GAP_ANGLE, false, paint)
      angle += sweepAngle
    }
  }

  private fun drawText(canvas: Canvas) {
    val text = createText()
    val textWidth = textPaint.measureText(text)
    canvas.drawText(text, (measuredWidth - textWidth) / 2f, (measuredHeight + TEXT_SIZE) / 2f, textPaint)
  }

  private fun createText(): String {
    if (accentSectionIndex !in model.items.indices) {
      return "Total amount: ${model.totalAmount}"
    }
    val accentItem = model.items[accentSectionIndex]
    return "${accentItem.name}: ${accentItem.amount}"
  }

  fun setAccentSection(index: Int) {
    this.accentSectionIndex = index
    invalidate()
  }

  fun resetAccentSection() {
    this.accentSectionIndex = NONE_SECTION_INDEX
    invalidate()
  }
}
