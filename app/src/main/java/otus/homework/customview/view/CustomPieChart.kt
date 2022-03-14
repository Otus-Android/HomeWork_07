package otus.homework.customview.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import otus.homework.customview.data.graphics.Palette
import otus.homework.customview.data.graphics.PiePoint
import otus.homework.customview.data.pie.ChartData
import otus.homework.customview.data.pie.OnSegmentClickListener
import otus.homework.customview.data.provider.DataSource

class CustomPieChart @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttrs: Int = 0
) : View(context, attrs, defStyleAttrs) {

  private val dataSource: DataSource = DataSource()
  private val chartData: ChartData = ChartData()

  private val paint = Paint()
  private val border = Paint()
  private val text = Paint()
  private val shape = RectF()
  private val selectedShape = RectF()
  private var selectedCategory: String? = null

  private var clickListener: OnSegmentClickListener? = null

  init {
    border.apply {
      style = Paint.Style.STROKE
      isAntiAlias = true
      color = Color.WHITE
    }

    text.apply {
      isAntiAlias = true
      color = Color.BLACK
      alpha = 0
    }

    paint.apply {
      style = Paint.Style.FILL
      isAntiAlias = true
    }
  }

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    val ws = MeasureSpec.getSize(widthMeasureSpec)
    val hs = MeasureSpec.getSize(heightMeasureSpec)
    val wm = MeasureSpec.getMode(widthMeasureSpec)
    val hm = MeasureSpec.getMode(heightMeasureSpec)

    val width = when (wm) {
      MeasureSpec.EXACTLY -> maxOf(minimumWidth, ws)
      MeasureSpec.AT_MOST -> minOf(minimumWidth, maxOf(minimumWidth, ws))
      else -> minimumWidth
    }

    val height = when (hm) {
      MeasureSpec.EXACTLY -> maxOf(minimumHeight, hs)
      MeasureSpec.AT_MOST -> minOf(minimumHeight, maxOf(minimumHeight, hs))
      else -> minimumHeight
    }

    setMeasuredDimension(width, height)
  }

  override fun getMinimumHeight(): Int {
    return (DEFAULT_SIZE * 0.7).toInt()
  }

  override fun getMinimumWidth(): Int {
    return DEFAULT_SIZE
  }

  override fun onDraw(canvas: Canvas?) {
    super.onDraw(canvas)
    chartData.segments.let { segments ->
      segments.forEach {
        if (selectedCategory == it.value.title) {
          canvas?.drawArc(
            selectedShape,
            it.value.startAngle,
            it.value.endAngle,
            true,
            it.value.paint
          )
        } else {
          canvas?.drawArc(shape, it.value.startAngle, it.value.endAngle, true, it.value.paint)
          canvas?.drawArc(shape, it.value.startAngle, it.value.endAngle, true, border)
        }
      }
    }
  }

  @SuppressLint("ClickableViewAccessibility")
  override fun onTouchEvent(event: MotionEvent): Boolean {
    val matched = PiePoint.locatedInChartWith(event.x, event.y, this)
    return when (event.action) {
      MotionEvent.ACTION_DOWN -> matched
      MotionEvent.ACTION_UP -> if (matched) {
        chartData
          .segments
          .values
          .find { segment ->
            (segment.startAngle..segment.startAngle + segment.endAngle).contains(
              PiePoint.angleByPoint(
                event.x,
                event.y,
                this
              )
            )
          }?.let {
            selectSegmentWith(category = it.title)
          }
        true
      } else super.onTouchEvent(event)
      else -> super.onTouchEvent(event)
    }
  }

  override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
    super.onSizeChanged(w, h, oldw, oldh)
    setBounds()
  }

  override fun onSaveInstanceState(): Parcelable {
    val bundle = Bundle()
    bundle.putParcelable("chart", super.onSaveInstanceState())
    bundle.putString("selected_category", selectedCategory)
    return bundle
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    ((state as Bundle).getString("selected_category"))?.let {
      selectSegmentWith(category = it)
    }
    super.onRestoreInstanceState(state)
  }

  private fun selectSegmentWith(category: String) {
    selectedCategory = category

    val amount = dataSource.mergedItemsByCategory[category] ?: 0

    clickListener?.action(category, amount)
    invalidate()
  }

  fun setOnSegmentClickListener(action: OnSegmentClickListener) {
    clickListener = action
  }

  fun setChartData() {
    var i = 0
    dataSource.providePieChartData().map { segment ->
      chartData.appendSegment(
        segment.key.category,
        segment.key.amount.toDouble(),
        Palette.colors[i]
      )
      i = if (i >= Palette.colors.size - 1) 0 else i + 1
    }
    setChart()
    requestLayout()
    invalidate()
  }

  private fun setChart() {
    var current = 0f
    chartData.segments.map {
      it.value.startAngle = current
      it.value.endAngle = ((it.value.data / chartData.total) * 360f).toFloat()
      current += it.value.endAngle
    }
  }

  private fun setBounds(
    top: Float = OFFSET,
    bottom: Float = layoutParams.height.toFloat() + OFFSET,
    left: Float = (width / 2 - layoutParams.height / 2).toFloat(),
    right: Float = (width / 2 + layoutParams.height / 2).toFloat()
  ) {
    selectedShape.top = top - OFFSET
    selectedShape.bottom = bottom + OFFSET
    selectedShape.left = left - OFFSET
    selectedShape.right = right + OFFSET
    shape.top = top
    shape.bottom = bottom
    shape.left = left
    shape.right = right
  }

  companion object {
    const val DEFAULT_SIZE = 900
    const val OFFSET = 20f
  }

}