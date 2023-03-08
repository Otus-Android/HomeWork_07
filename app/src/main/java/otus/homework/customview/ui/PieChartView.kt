package otus.homework.customview.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import otus.homework.customview.R
import kotlin.math.*

// strokes
private const val STROKE_WIDTH = 100f
private const val ACCENT_RATIO = 1.8f
private const val ACCENT_STROKE_WIDTH = ACCENT_RATIO * STROKE_WIDTH

// sections
private const val GAP_ANGLE = 1.5f
private const val NONE_SECTION_INDEX = -1

// text
private const val TEXT_SIZE = 96f
private const val TEXT_COLOR = Color.BLACK

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

  private var model = PieChartModel(emptyList())

  private var accentSectionIndex = NONE_SECTION_INDEX

  private val rect = RectF()

  private val paint = Paint()
    .apply {
      style = Paint.Style.STROKE
      strokeWidth = STROKE_WIDTH
    }

  private val textPaint = Paint()
    .apply {
      color = TEXT_COLOR
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
    model.sections.forEachIndexed { index, section ->
      paint.color = COLORS[index % COLORS.size]
      paint.strokeWidth = if (index == accentSectionIndex) {
        ACCENT_STROKE_WIDTH
      } else {
        STROKE_WIDTH
      }
      canvas.drawArc(rect, -90f + section.startAngle, section.sweepAngle - GAP_ANGLE, false, paint)
    }
  }

  private fun drawText(canvas: Canvas) {
    if (model.items.isEmpty()) return
    val text = createText()
    val textWidth = textPaint.measureText(text)
    canvas.drawText(text, (measuredWidth - textWidth) / 2f, (measuredHeight + TEXT_SIZE) / 2f, textPaint)
  }

  private fun createText(): String {
    if (accentSectionIndex !in model.items.indices) {
      return context.getString(R.string.total_amount, "${model.totalAmount}")
    }
    val accentItem = model.items[accentSectionIndex]
    return "${accentItem.name}: ${accentItem.value}"
  }

  @SuppressLint("ClickableViewAccessibility")
  override fun onTouchEvent(event: MotionEvent): Boolean {
    val x = event.x - measuredWidth / 2
    val y = event.y - measuredHeight / 2

    val radius = sqrt(x * x + y * y)
    var angle = Math.toDegrees(atan2(y.toDouble(), x.toDouble())) + 90f
    if (angle < 0) angle += 360f

    if (radius in (measuredWidth / 2f - ACCENT_STROKE_WIDTH)..(measuredWidth / 2f)) {
      val section = model.sections.find { it.startAngle <= angle && angle <= it.endAngle }
      accentSectionIndex = model.sections.indexOf(section)
    } else {
      accentSectionIndex = NONE_SECTION_INDEX
    }
    invalidate()

    return true
  }

  fun updateData(model: PieChartModel) {
    this.model = model
    this.accentSectionIndex = NONE_SECTION_INDEX
    invalidate()
  }
}
