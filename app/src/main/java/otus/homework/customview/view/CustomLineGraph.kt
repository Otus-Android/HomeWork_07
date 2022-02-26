package otus.homework.customview.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import otus.homework.customview.data.graph.*
import otus.homework.customview.data.provider.DataSource
import otus.homework.customview.util.toDateString
import otus.homework.customview.util.toDay
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.pow

class CustomLineGraph @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttrs: Int = 0,
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
      color = Color.GRAY
      strokeWidth = 6f
    }

    graphStartDate = 0L
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

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    setGraphDimensions()
    drawAxis(canvas)
    drawAxisMarkers(canvas)
    drawGraph(canvas)
  }

  fun setGraphData(forCategory: String) {
    graphData.items.clear()
    dataSource.provideLineGraphData()[forCategory]?.first?.map {
      graphData.appendItem(it.name, it.amount, it.time)
    }
    graphData.items.sortedBy { it.timestamp }.let {
      val min = it.first().timestamp
      val max = it.last().timestamp
      val intervalInDays = TimeUnit.SECONDS.toDays(max - min)
      graphStartDate = min.toDay()
      timeStep = TimeUnit.DAYS.toSeconds(maxOf(intervalInDays.toInt() / 5, 1).toLong())
    }
    setAxisValues()
    requestLayout()
    invalidate()
  }

  private fun setGraphDimensions() {
    startX = 140f
    startY = height - 40f
    xStep = width / 7f
    yStep = height / 7f
    text.textSize = height / 20f
  }

  private fun drawAxis(canvas: Canvas) {
    if (graphData.items.isEmpty()) return
    canvas.drawLine(startX, 30f, startX, height - 30f, axis)
    canvas.drawLine(startX - 20f, startY, width - 20f, height - 40f, axis)
  }

  private fun drawGraph(canvas: Canvas) {
    path.reset()
    path.moveTo(startX, startY)
    graphData.items.forEachIndexed { index, categoryDetail ->

      var x = (categoryDetail.timestamp - graphStartDate) * xStep / timeStep * 10
      val y = categoryDetail.sum * yStep / (amountStep * 1000)
      x = if (x > 320f) 280f else  x
      if (index == 0) {
        Log.d("Coords", "for index $index => cx: $x + $startX, cy: $startY - $y, time: ${categoryDetail.timestamp.toDateString()}")
        path.lineTo(x + startX,  abs(startY - y))
        canvas.drawCircle(x + startX, abs(startY - y), 6f, graph)
      } else {
        Log.d("Coords", "for index $index => cx: $x + $startX, cy: $startY - $y, time: ${categoryDetail.timestamp.toDateString()}")
        path.lineTo(x + startX,  abs(startY - y))
        canvas.drawCircle(x + startX, abs(startY - y), 6f, graph)
      }
    }
    canvas.drawPath(path, graph)
  }

  private fun setAxisValues() {
    if (graphData.items.isEmpty()) return

    val degree = log10(graphData.items.maxByOrNull { it.sum }!!.sum.toDouble()).toInt()
    yExpFactor = 10.0.pow(degree.toDouble()).toInt()
    yStep = 2f * yExpFactor

    graphData.items.sortBy { it.timestamp }
    val days =
      TimeUnit.SECONDS.toDays(graphData.items.last().timestamp - graphData.items.first().timestamp)
    xExpFactor = maxOf(days.toInt() / 5, 1)
    xStep = TimeUnit.HOURS.toSeconds(xExpFactor.toLong()).toFloat()
    graphStartDate = graphData.items.first().timestamp.toDay()
  }

  private fun drawAxisMarkers(canvas: Canvas) {
    if (graphData.items.isEmpty()) return
    var startX = this.startX + xStep
    var stopY = height - 40f
    for (i in 1..5) {
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
      canvas.drawText("${if (i == 5) 3 * i * yExpFactor else 2 * i * yExpFactor}", 30f, stopY - 10f, text)
      stopY -= yStep
    }

    val xAxisText = "Дата"
    val widthText = text.measureText(xAxisText)
    canvas.drawText(xAxisText, width - widthText - 10f, height - 60f, text)
  }

  companion object {
    const val DEFAULT_SIZE = 900
    const val OFFSET = 20f
  }

}