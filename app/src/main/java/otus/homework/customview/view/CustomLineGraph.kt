package otus.homework.customview.view

import android.animation.Animator
import android.content.Context
import android.content.res.TypedArray
import android.database.DataSetObserver
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import otus.homework.customview.R
import otus.homework.customview.data.graph.*
import otus.homework.customview.data.provider.DataSource
import otus.homework.customview.util.GraphDataSet
import otus.homework.customview.util.toDateString
import otus.homework.customview.util.toDay
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.pow

class CustomLineGraph @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttrs: Int = R.attr.LineChartStyle,
  defStyleRes: Int = R.style.CustomLineGraph
) : View(context, attrs, defStyleAttrs), PathAnimatable {

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


  @ColorInt
  var lineColor: Int = 0
    set(value) {
      field = value
      linePaint.color = lineColor
      invalidate()
    }

  @ColorInt
  var fillColor: Int = 0
    set(value) {
      field = value
      setupFillPaint(value)
      invalidate()
    }

  private var lineWidth: Float = 0.toFloat()
    set(value) {
      field = value
      linePaint.strokeWidth = field
      invalidate()
    }

  private var linePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    set(value) {
      field = value
      invalidate()
    }

  private var fillPaint = Paint(Paint.ANTI_ALIAS_FLAG and Paint.FILTER_BITMAP_FLAG)
    set(value) {
      field = value
      invalidate()
    }

  private var cornerRadius: Float = 0.toFloat()
    set(value) {
      field = value
      if (field != 0f) {
        linePaint.pathEffect = CornerPathEffect(field)
        fillPaint.pathEffect = CornerPathEffect(field)
      } else {
        linePaint.pathEffect = null
        fillPaint.pathEffect = null
      }
      invalidate()
    }

  private val dataSetObserver = object :DataSetObserver() {
    override fun onChanged() {
      super.onChanged()
      populatePathAndAnimate()
    }

    override fun onInvalidated() {
      super.onInvalidated()
      clearData()
    }
  }

  private var animateChanges: Boolean = false
    set(value) {
      field = value
      configureAnimation()
    }

  private var adapter: LineGraphAdapter? = null
    set(value) {
      field?.unregisterDataSetObserver(dataSetObserver)
      field = value
      field?.registerDataSetObserver(dataSetObserver)
      populatePathAndAnimate()
    }

  private val contentRect = RectF()
  private val linePath = Path()
  private val renderPath = Path()
  private var scaleHelper: ScaleHelper? = null
  private var chartAnimator: ChartAnimator? = null
  private var pathAnimator: Animator? = null


  private var animationDelay: Int = 0
    set(value) {
      field = value
      invalidate()
    }

  @FillType
  private var fillType = FillType.NONE
    set(value) {
      field = value
      populatePath()
    }

  private val fillEdge: Float?
    get() {
      return when (fillType) {
        FillType.NONE -> null
        FillType.UP -> contentRect.top
        FillType.DOWN -> contentRect.bottom + FILL_PADDING_DOWN
        FillType.TOWARD_ZERO -> {
          val zero = scaleHelper!!.getY(0f)
          val bottom = contentRect.bottom
          zero.coerceAtMost(bottom)
        }
        else ->
          throw IllegalStateException(String.format(Locale.US, "Unknown fill type: %d", fillType))
      }
    }

  private val isFillInternal: Boolean
    get() = isFilled

  private var isGradientFill: Boolean = false
    set(value) {
      field = value
      setupFillPaint(fillColor)
      invalidate()
    }

  private var isFilled: Boolean
    get() {
      return when (fillType) {
        FillType.UP, FillType.DOWN, FillType.TOWARD_ZERO -> true
        else -> false
      }
    }
    set(fill) {
      fillType = when (fill) {
        true -> FillType.DOWN
        else -> FillType.NONE
      }
    }

  init {
    val a = context.obtainStyledAttributes(attrs, R.styleable.CustomLineGraph, defStyleAttrs, defStyleRes)
    configureAttr(a)
    a.recycle()
    configurePaints()

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
    return (CustomPieChart.DEFAULT_SIZE * 0.7).toInt()
  }

  override fun getMinimumWidth(): Int {
    return CustomPieChart.DEFAULT_SIZE
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    setGraphDimensions()
    drawAxis(canvas)
    drawAxisMarkers(canvas)
    drawGraph(canvas)
//    drawLineGraph(canvas)
  }

  override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
    super.onSizeChanged(w, h, oldw, oldh)
    updateContentRect()
    populatePath()
  }

  override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
    super.setPadding(left, top, right, bottom)
    updateContentRect()
    populatePath()
  }

  override fun setAnimationPath(animationPath: Path) {
    renderPath.reset()
    renderPath.addPath(animationPath)
    renderPath.rLineTo(0f, 0f)
    invalidate()
  }

  override fun getPath(): Path = linePath

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
    setAdapter()
    setAxisValues()
    requestLayout()
    invalidate()
  }

  private fun configureAttr(a: TypedArray) {
    a.run {
      lineColor = getColor(
        R.styleable. CustomLineGraph_linechart_lineColor,
        ContextCompat.getColor(context, R.color.purple_200)
      )

      fillColor = getColor(
        R.styleable.CustomLineGraph_linechart_fillColor,
        ContextCompat.getColor(context, R.color.purple_200)
      )

      lineWidth = getDimension(R.styleable.CustomLineGraph_linechart_lineWidth, 0f)
      cornerRadius = getDimension(R.styleable.CustomLineGraph_linechart_cornerRadius, 0f)
      isGradientFill = a.getBoolean(R.styleable.CustomLineGraph_linechart_gradientFill, false)
      animationDelay = getInt(R.styleable.CustomLineGraph_linechart_aimationDelay, 0)
      animateChanges = getBoolean(R.styleable.CustomLineGraph_linechart_animateChanges, false)
      fillType = getInt(R.styleable.CustomLineGraph_linechart_fillType, FillType.NONE)
    }
  }

  private fun configurePaints() {
    linePaint.style = Paint.Style.STROKE
    linePaint.color = lineColor
    linePaint.strokeWidth = lineWidth
    linePaint.strokeMiter = 2f
    linePaint.strokeCap = Paint.Cap.SQUARE
    linePaint.pathEffect = CornerPathEffect(0f)
    setupFillPaint(fillColor)
  }

  private fun setupFillPaint(@ColorInt color : Int){
    fillPaint.style = Paint.Style.FILL
    if (!isGradientFill) {
      fillPaint.color = color
    } else {
      setGradientFillColor(color)
    }
  }

  private fun setGradientFillColor(color: Int) {
    val gradient = LinearGradient(
      0f,
      0f,
      0f,
      contentRect.top,
      color,
      Color.TRANSPARENT,
      Shader.TileMode.MIRROR
    )
    fillPaint.shader = gradient
  }

  private fun clearData() {
    scaleHelper = null
    renderPath.reset()
    linePath.reset()
    invalidate()
  }

  private fun configureAnimation() {
    if (animateChanges)
      chartAnimator = ChartAnimator()
  }

  private  fun getYByIndex(index: Int) = scaleHelper!!.getY(adapter!!.getY(index))

  private fun populatePath() {
    if (adapter == null || width == 0 || height == 0) return
    linePath.moveTo(startX, startY)
    scaleHelper = ScaleHelper(adapter!!, contentRect, 4f, isFillInternal)
    for (i in 0 until adapter!!.count) {
      if (i < adapter!!.count) {
        val x = scaleHelper!!.getX(adapter!!.getX(i))
        val y = scaleHelper!!.getY(adapter!!.getY(i))
        if (i == 0)
          linePath.moveTo(x, y)
        else
          linePath.lineTo(x, y)
      }
    }

    fillEdge?.let {
      val lastX = scaleHelper!!.getX((adapter!!.count - 1).toFloat())
      val yByIndex = getYByIndex(adapter!!.count - 1)
      linePath.lineTo(lastX, yByIndex)
      linePath.lineTo(contentRect.right + FILL_PADDING_END, yByIndex)
      linePath.lineTo(contentRect.right + FILL_PADDING_END, it)
      linePath.lineTo(contentRect.left, it)
    }

    renderPath.reset()
    renderPath.addPath(linePath)
    invalidate()
  }

  private fun populatePathAndAnimate() {
    populatePath()
    if (chartAnimator != null)
      animatePath()
  }

  private fun animatePath() {
    pathAnimator?.cancel()
    pathAnimator = chartAnimator?.getAnimation(this)
    pathAnimator?.start()
  }

  private fun updateContentRect() {
    contentRect.set(
      paddingStart.toFloat(),
      paddingTop.toFloat(),
      (width - paddingEnd).toFloat(),
      (height - paddingBottom).toFloat()
    )
  }

  private fun setAdapter() {
    val list: GraphDataSet = arrayListOf()
    graphData.items.forEach {
      val x = (it.timestamp - graphStartDate) * xStep / timeStep * 10
      val y = it.sum * yStep / (amountStep * 1000)
      list.add(Pair(x, y))
    }
    adapter?.setData(list)
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

  private fun drawLineGraph(canvas: Canvas) {
    if (isFilled)
      canvas.drawPath(renderPath, fillPaint)
    else
      canvas.drawPath(renderPath, linePaint)
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
    const val FILL_PADDING_DOWN = 10f
    const val FILL_PADDING_END = 100f
  }
}