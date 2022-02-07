package otus.homework.customview.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import otus.homework.customview.data.graph.GraphData
import otus.homework.customview.data.provider.DataSource
import otus.homework.customview.util.toDateString
import otus.homework.customview.util.toDay
import java.util.concurrent.TimeUnit
import kotlin.math.log10
import kotlin.math.pow

class CustomLineGraph @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttrs: Int = 0
) : View(context, attrs, defStyleAttrs) {

  private val dataSource = DataSource()
  private val graphData = GraphData()

  private val axis = Paint()
  private val text = Paint()
  private val graph = Paint()

  private val path = Path()

  private var graphStartDate: Long // = 0L
  private var timeStep = 1L // x-axis
  private var amountStep = 1 // y-axis
  private var startX = 0f
  private var startY = 0f
  private var xStep = 0f
  private var yStep = 0f
  private var xExpFactor = 0
  private var yExpFactor = 0

  init {
    axis.apply {
      style = Paint.Style.STROKE
      isAntiAlias = true
      color = Color.BLACK
      strokeWidth = 4f
    }

    text.apply {
      color = Color.BLACK
      textAlign = Paint.Align.LEFT
      isAntiAlias = true
    }

    graph.apply {
      style = Paint.Style.STROKE
      isAntiAlias = true
      color = Color.RED
      strokeWidth = 6f
    }

    graphStartDate = System.currentTimeMillis() / 1000
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
    return (CustomPieChart.DEFAULT_SIZE * 0.7).toInt()
  }

  override fun getMinimumWidth(): Int {
    return CustomPieChart.DEFAULT_SIZE
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    setGraphDimensions()
    drawAxisMarkers(canvas)
    drawGraph(canvas)
  }

  fun setGraphData(forCategory: String) {
    graphData.items.clear()
    dataSource.provideLineGraphData()[forCategory]?.first?.map {
      graphData.appendItem(it.name, it.amount, it.time)
    }
    graphData.items.sortBy { it.timestamp }
    graphData.items.let {
      val min = it.first().timestamp
      val max = it.last().timestamp
      val intervalInDays = TimeUnit.SECONDS.toDays(max - min)
      graphStartDate = min.toDay()
      timeStep = TimeUnit.DAYS.toSeconds(maxOf(intervalInDays.toInt() / 5, 1).toLong())
    }
    setAxisOrder()
    invalidate()
  }

  private fun setGraphDimensions() {
    startX = 140f
    startY = height - 40f
    xStep = width / 7f
    yStep = height / 7f
    text.textSize = height / 20f
  }

  private fun drawGraph(canvas: Canvas) {
    canvas.drawLine(startX, 30f, startX, height - 30f, axis)
    canvas.drawLine(startX - 20f, startY, width - 20f, height - 40f, axis)

    path.reset()
    path.moveTo(startX, startY)

    graphData.items.forEachIndexed { index, categoryDetail ->
      val x = (categoryDetail.timestamp - graphStartDate) * xStep / timeStep
      val y = categoryDetail.sum * yStep / amountStep
      if (index == 0) {
        path.moveTo(x + startX, startY - y)
        canvas.drawCircle(x + startX, startY - y, 6f, graph)
      } else {
        path.lineTo(x + startX, startY - y)
        canvas.drawCircle(x + startX, startY - y, 6f, graph)
      }
    }
    canvas.drawPath(path, graph)
  }

  private fun setAxisOrder() {
    if (graphData.items.isEmpty()) return
    graphData.items.sortByDescending { it.sum }

    val degree = log10(graphData.items.first().sum.toDouble()).toInt()
    yExpFactor = 10.0.pow(degree.toDouble()).toInt()
    yStep = 2f * yExpFactor

    graphData.items.sortBy { it.timestamp }
    val days =
      TimeUnit.SECONDS.toDays(graphData.items.last().timestamp - graphData.items.first().timestamp)
    xExpFactor = maxOf(days.toInt() / 5, 1)
    xStep = TimeUnit.DAYS.toSeconds(xExpFactor.toLong()).toFloat()
    graphStartDate = graphData.items.first().timestamp.toDay()
  }

  private fun drawAxisMarkers(canvas: Canvas) {
    if (graphData.items.isEmpty()) return
    var startX = this.startX + xStep
    var stopY = height - 40f
    for (i in 0 until 5) {

      val axisText =
        (graphStartDate + TimeUnit.DAYS.toSeconds((i * xExpFactor).toLong())).toDateString()

      val widthText = text.measureText(axisText)
      canvas.drawLine(startX, height - 60f, startX, height - 40f, text)
      canvas.drawText(axisText, startX - widthText / 2, height.toFloat(), text)
      startX += xStep
    }

    val yAxisText = "Траты, руб"
    canvas.drawText(yAxisText, 10f, 22f, text)

    startX = 2 * 20f
    stopY -= yStep
    for (i in 1..5) {
      val widthText = text.measureText("${10 * yStep}") + 10f
      canvas.drawLine(startX + 100f, stopY, widthText + 80f, stopY, text)
      canvas.drawText("${2 * i * yExpFactor}", 30f, stopY - 10f, text)
      stopY -= yStep
    }

    val xAxisText = "Дата"
    val widthText = text.measureText(xAxisText)
    canvas.drawText(xAxisText, width - widthText - 10f, height - 60f, text)
  }

  @SuppressLint("ClickableViewAccessibility")
  override fun onTouchEvent(event: MotionEvent?): Boolean {
    return super.onTouchEvent(event)
  }

  override fun onSaveInstanceState(): Parcelable? {
    return super.onSaveInstanceState()
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    super.onRestoreInstanceState(state)
  }
}