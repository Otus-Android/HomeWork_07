package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.lifecycle.MutableLiveData
import kotlin.math.atan2

class PieChartView
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : CustomVew(context, attrs) {

  private val selectedPayload = MutableLiveData<PayloadUiModel>()

  private val blackPaint =
      Paint().apply {
        color = Color.BLACK
        strokeWidth = STROKE_WIDTH
        style = Paint.Style.FILL
      }

  private val greenPaint =
      Paint().apply {
        color = Color.GREEN
        strokeWidth = STROKE_WIDTH
        style = Paint.Style.FILL
      }

  private val redPaint =
      Paint().apply {
        color = Color.RED
        strokeWidth = STROKE_WIDTH
        style = Paint.Style.FILL
      }
  private val yellowPaint =
      Paint().apply {
        color = Color.YELLOW
        strokeWidth = STROKE_WIDTH
        style = Paint.Style.FILL
      }
  private val grayPaint =
      Paint().apply {
        color = Color.GRAY
        strokeWidth = STROKE_WIDTH
        style = Paint.Style.FILL
      }
  private val bluePaint =
      Paint().apply {
        color = Color.BLUE
        strokeWidth = STROKE_WIDTH
        style = Paint.Style.FILL
      }
  private val purplePaint =
      Paint().apply {
        color = context.getColor(R.color.purple_200)
        strokeWidth = STROKE_WIDTH
        style = Paint.Style.FILL
      }
  private val lightBluePaint =
      Paint().apply {
        color = context.getColor(R.color.teal_200)
        strokeWidth = STROKE_WIDTH
        style = Paint.Style.FILL
      }
  private val orangePaint =
      Paint().apply {
        color = context.getColor(R.color.orange)
        strokeWidth = STROKE_WIDTH
        style = Paint.Style.FILL
      }
  private val lightPinkPaint =
      Paint().apply {
        color = context.getColor(R.color.light_pink)
        strokeWidth = STROKE_WIDTH
        style = Paint.Style.FILL
      }
  private val orangeRedPaint =
      Paint().apply {
        color = context.getColor(R.color.orange_red)
        strokeWidth = STROKE_WIDTH
        style = Paint.Style.FILL
      }
  private val olivePaint =
      Paint().apply {
        color = context.getColor(R.color.olive)
        strokeWidth = STROKE_WIDTH
        style = Paint.Style.FILL
      }

  private val rect = RectF()

  init {
    isSaveEnabled = true
  }

  override fun onSaveInstanceState(): Parcelable {
    super.onSaveInstanceState()
    val bundle =
        Bundle().apply {
          putParcelable(SELECTED_PAYLOADS, selectedPayload.value)
        }
    return bundle
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    if (state is Bundle) {
        state.getParcelable<PayloadUiModel>(SELECTED_PAYLOADS)?.let { selectedPayload.value = it }

    }
    super.onRestoreInstanceState(state)
  }


  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    var totalAmount = listPayload.sumOf { it.amount }

    val rectSize = RECT_SIZE
    val left = (width - rectSize) / 2
    val top = (height - rectSize) / 2
    val right = left + rectSize
    val bottom = top + rectSize
    rect.set(left, top, right, bottom)
    var startAngle = 0f

    listPayload.forEach { payload ->
      val sweepAngle = (payload.amount.toFloat() / totalAmount) * CIRCLE_DEGREE

      val paint = getPainter(payload)

      canvas.drawArc(rect, startAngle, sweepAngle, true, paint)

      startAngle += sweepAngle
    }

    selectedPayload.value?.let { payload ->
      val text = payload.name
      blackPaint.textSize = TEXT_SIZE
      blackPaint.textAlign = Paint.Align.LEFT
      canvas.drawText(text, 50f, 200f, blackPaint)
    }
  }

  override fun onTouchEvent(event: MotionEvent): Boolean {
    if (event.action == MotionEvent.ACTION_DOWN) {
      val x = event.x - width / 2f
      val y = event.y - height / 2f

      if (rect.contains(event.x, event.y)) {
        var touchAngle = Math.toDegrees(atan2(y.toDouble(), x.toDouble())).toFloat()
        touchAngle = if (touchAngle < 0) touchAngle + CIRCLE_DEGREE else touchAngle

        var startAngle = 0f
        val totalAmount = listPayload.sumOf { it.amount }
        for (payload in listPayload) {
          val sweepAngle = (payload.amount.toFloat() / totalAmount) * CIRCLE_DEGREE
          if (touchAngle >= startAngle && touchAngle < startAngle + sweepAngle) {
            selectedPayload.value = payload
            invalidate()
          }
          startAngle += sweepAngle
        }
      }
    }
    return true

  }

  private fun getPainter(payload: PayloadUiModel): Paint {
        return when (payload.id) {
          1 -> blackPaint
          2 -> grayPaint
          3 -> bluePaint
          4 -> redPaint
          5 -> greenPaint
          6 -> yellowPaint
          7 -> lightBluePaint
          8 -> lightPinkPaint
          9 -> orangePaint
          10 -> purplePaint
          11 -> orangeRedPaint
          else -> olivePaint
        }
  }

  companion object {
    private const val SELECTED_PAYLOADS = "selectedPayload"
    private const val CIRCLE_DEGREE = 360
    private const val STROKE_WIDTH = 10f
    private const val RECT_SIZE = 1000f
    private const val TEXT_SIZE = 100f
  }
}
