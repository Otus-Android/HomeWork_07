package otus.homework.customview

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random


@SuppressLint("DrawAllocation")
class PieChartView : View, View.OnTouchListener {
    private var onPieClickListener: OnPieClickListener? = null

    private val piePaint: Paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    private val linePaint: Paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
    }
    private val textPaint: Paint = Paint().apply {
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    private var lineColor = 0
    private var lineStrokeWidth = DEFAULT_LINE_STROKE_WIDTH
    private var diameter = 0
    private var radius = 0

    private val mutableState = MutableLiveData<PieChartState>(PieChartState.Idle)

    private val gson = Gson()

    companion object {
        private const val DEFAULT_LINE_STROKE_WIDTH = 3.0f
        private const val DEFAULT_PIE_HEIGHT = 100f
        private const val DEFAULT_OFFSET = 10f
    }

    constructor(context: Context) : super(context) {
        lineColor = context.getColor(R.color.default_line_color)

        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        val ta: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.PieChartView, 0, 0)
        try {
            lineColor = ta.getColor(
                R.styleable.PieChartView_lineDefaultColor,
                context.getColor(R.color.default_line_color)
            )
            lineStrokeWidth =
                ta.getFloat(R.styleable.PieChartView_lineStrokeWidth, DEFAULT_LINE_STROKE_WIDTH)
        } finally {
            ta.recycle()
        }

        init()
    }

    private fun init() {
        getPayloadFromAsset(context)
        setOnTouchListener(this)
    }

    private fun getPayloadFromAsset(context: Context) {
        try {
            val jsonString = context.resources.openRawResource(R.raw.payload).bufferedReader()
                .use { it.readText() }
            val pieChartItems: MutableList<PieChartItem> = mutableListOf()
            pieChartItems.addAll(
                gson.fromJson(
                    jsonString,
                    object : TypeToken<List<PieChartItem>>() {}.type
                )
            )
            mutableState.value = PieChartState.Init(pieChartItems)
        } catch (exception: IOException) {
            exception.printStackTrace()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = suggestedMinimumWidth + paddingLeft + paddingRight
        val desiredHeight = suggestedMinimumHeight + paddingTop + paddingBottom

        val resultWidth = measureDimension(desiredWidth, widthMeasureSpec)
        val resultHeight = measureDimension(desiredHeight, heightMeasureSpec)
        setMeasuredDimension(
            resultWidth.coerceAtMost(resultHeight),
            resultWidth.coerceAtMost(resultHeight)
        )
    }

    private fun measureDimension(desiredSize: Int, measureSpec: Int): Int {
        var result: Int
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)
        if (specMode == MeasureSpec.EXACTLY || desiredSize == 0) {
            result = specSize
        } else {
            result = desiredSize
            if (specMode == MeasureSpec.AT_MOST) {
                result = result.coerceAtMost(specSize)
            }
        }

        return result
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawColor(Color.WHITE)

        linePaint.color = lineColor
        linePaint.strokeWidth = lineStrokeWidth

        textPaint.textSize = 40f // sp to px convert needed

        diameter = measuredWidth
        radius = diameter / 2
        if (measuredHeight < diameter) {
            diameter = measuredHeight
        }

        val paddingVertical: Int = (measuredHeight - diameter) / 2
        val paddingHorizontal: Int = (measuredWidth - diameter) / 2
        val pieRect = RectF(
            paddingLeft.toFloat() + DEFAULT_OFFSET,
            paddingTop.toFloat() + DEFAULT_OFFSET,
            diameter - paddingRight.toFloat() - DEFAULT_OFFSET,
            diameter - paddingBottom.toFloat() - DEFAULT_OFFSET
        )
        pieRect.offsetTo(
            paddingHorizontal + paddingLeft.toFloat() + DEFAULT_OFFSET,
            paddingVertical + paddingTop.toFloat() + DEFAULT_OFFSET
        )

        val piePoints: MutableList<PieChartCategoryPoint> = mutableListOf()

        when (val currentState = mutableState.value) {
            PieChartState.Idle -> {}
            is PieChartState.DrawComplete -> {
                currentState.piePoints.forEach {
                    piePaint.color = it.color
                    canvas.drawArc(pieRect, it.start, it.sweep, true, piePaint)

                    val path = Path()
                    path.addArc(pieRect, it.start, it.sweep)
                    val maxTextWidth =
                        (diameter * sin(Math.toRadians(it.sweep.toDouble()) / 2)).toFloat()
                    do {
                        val maxCount =
                            textPaint.breakText(it.category, true, maxTextWidth, FloatArray(1))
                        if (maxCount < it.category.length)
                            textPaint.textSize = textPaint.textSize - 5
                    } while (maxCount < it.category.length)
                    canvas.drawTextOnPath(it.category, path, 0f, textPaint.textSize, textPaint)

                    canvas.drawArc(pieRect, it.start, it.sweep, true, linePaint)
                }
            }
            is PieChartState.Init -> {
                var start = 0f
                var sweep: Float

                currentState.pieChartItems.groupBy { it.category }.forEach { (category, list) ->

                    val amount = list.sumBy { it.amount }
                    sweep = 360f * (amount / currentState.maxConnection.toFloat())

                    val color = Color.rgb(
                        Random.Default.nextInt(256),
                        Random.Default.nextInt(256),
                        Random.Default.nextInt(256)
                    )
                    piePaint.color = color
                    canvas.drawArc(pieRect, start, sweep, true, piePaint)

                    val path = Path()
                    path.addArc(pieRect, start, sweep)
                    val maxTextWidth =
                        (diameter * sin(Math.toRadians(sweep.toDouble()) / 2)).toFloat()
                    do {
                        val maxCount =
                            textPaint.breakText(category, true, maxTextWidth, FloatArray(1))
                        if (maxCount < category.length)
                            textPaint.textSize = textPaint.textSize - 5
                    } while (maxCount < category.length)
                    canvas.drawTextOnPath(category, path, 0f, textPaint.textSize, textPaint)

                    canvas.drawArc(pieRect, start, sweep, true, linePaint)

                    piePoints.add(
                        PieChartCategoryPoint(
                            category = category,
                            polygon = Polygon.Builder()
                                .addVertex(
                                    PointF(
                                        (pieRect.centerX() + radius * cos(Math.toRadians(start.toDouble()))).toFloat(),
                                        (pieRect.centerY() + radius * sin(Math.toRadians(start.toDouble()))).toFloat()
                                    )
                                )
                                .addVertex(
                                    PointF(
                                        (pieRect.centerX() + (radius - DEFAULT_PIE_HEIGHT) * cos(
                                            Math.toRadians(start.toDouble())
                                        )).toFloat(),
                                        (pieRect.centerY() + (radius - DEFAULT_PIE_HEIGHT) * sin(
                                            Math.toRadians(start.toDouble())
                                        )).toFloat()
                                    )
                                )
                                .addVertex(
                                    PointF(
                                        (pieRect.centerX() + (radius - DEFAULT_PIE_HEIGHT) * cos(
                                            Math.toRadians((start + sweep / 2).toDouble())
                                        )).toFloat(),
                                        (pieRect.centerY() + (radius - DEFAULT_PIE_HEIGHT) * sin(
                                            Math.toRadians((start + sweep / 2).toDouble())
                                        )).toFloat()
                                    )
                                )
                                .addVertex(
                                    PointF(
                                        (pieRect.centerX() + (radius - DEFAULT_PIE_HEIGHT) * cos(
                                            Math.toRadians((start + sweep).toDouble())
                                        )).toFloat(),
                                        (pieRect.centerY() + (radius - DEFAULT_PIE_HEIGHT) * sin(
                                            Math.toRadians((start + sweep).toDouble())
                                        )).toFloat()
                                    )
                                )
                                .addVertex(
                                    PointF(
                                        (pieRect.centerX() + radius * cos(Math.toRadians((start + sweep).toDouble()))).toFloat(),
                                        (pieRect.centerY() + radius * sin(Math.toRadians((start + sweep).toDouble()))).toFloat()
                                    )
                                )
                                .addVertex(
                                    PointF(
                                        (pieRect.centerX() + radius * cos(Math.toRadians((start + sweep / 2).toDouble()))).toFloat(),
                                        (pieRect.centerY() + radius * sin(Math.toRadians((start + sweep / 2).toDouble()))).toFloat()
                                    )
                                )
                                .build(),
                            color = color,
                            sweep = sweep,
                            start = start
                        )
                    )

                    canvas.drawArc(pieRect, start, sweep, true, linePaint)

                    start += sweep
                }

                mutableState.value = PieChartState.DrawComplete(piePoints)
            }
        }

        piePaint.color = Color.WHITE
        canvas.drawCircle(
            pieRect.centerX(),
            pieRect.centerY(),
            radius - DEFAULT_PIE_HEIGHT,
            piePaint
        )
        canvas.drawCircle(
            pieRect.centerX(),
            pieRect.centerY(),
            radius - DEFAULT_PIE_HEIGHT,
            linePaint
        )
    }

    override fun onTouch(view: View?, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                return true
            }
            MotionEvent.ACTION_UP -> {
                val currentState = mutableState.value
                if (currentState is PieChartState.DrawComplete)
                    currentState.piePoints.find {
                        it.polygon.contains(
                            PointF(
                                event.x,
                                event.y
                            )
                        )
                    }?.category?.let {
                        onPieClickListener?.onCategoryClick(it)
                    }
                return true
            }
        }
        return false
    }

    fun setOnPieClickListener(listener: ((String) -> Unit)) {
        onPieClickListener = object : OnPieClickListener {
            override fun onCategoryClick(category: String) {
                listener.invoke(category)
            }
        }
    }
}