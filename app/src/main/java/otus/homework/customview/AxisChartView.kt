package otus.homework.customview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.CornerPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException
import kotlin.random.Random


@SuppressLint("DrawAllocation")
class AxisChartView : View {

    private val linePaint: Paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        pathEffect = CornerPathEffect(200f)
    }
    private val textPaint: Paint = Paint().apply {
        isAntiAlias = true
    }

    private var lineStrokeWidth = DEFAULT_LINE_STROKE_WIDTH

    private val mutableState = MutableLiveData<AxisChartState>(AxisChartState.Idle)

    private val gson = Gson()

    companion object {
        private const val DEFAULT_LINE_STROKE_WIDTH = 8.0f
        private const val DEFAULT_OFFSET = 10f
        private const val TEXT_AXIS_OFFSET = 60f
    }

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    private fun init() {
        getPayloadFromAsset(context)
    }

    private fun getPayloadFromAsset(context: Context) {
        try {
            val jsonString = context.resources.openRawResource(R.raw.payload).bufferedReader()
                .use { it.readText() }
            mutableState.value = AxisChartState.Init(
                gson.fromJson(
                    jsonString,
                    object : TypeToken<List<PieChartItem>>() {}.type
                )
            )
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

        linePaint.strokeWidth = lineStrokeWidth

        textPaint.textSize = 20f // sp to px convert needed

        val rect = RectF(
            paddingLeft.toFloat() + DEFAULT_OFFSET,
            paddingTop.toFloat() + DEFAULT_OFFSET,
            measuredWidth - paddingRight.toFloat() - TEXT_AXIS_OFFSET,
            measuredHeight - paddingBottom.toFloat() - DEFAULT_OFFSET
        )
        rect.offsetTo(
            paddingLeft.toFloat() + DEFAULT_OFFSET,
            paddingTop.toFloat() + DEFAULT_OFFSET
        )

        val maxAmount = when (val currentState = mutableState.value) {
            is AxisChartState.DrawComplete -> currentState.maxAmount
            is AxisChartState.Init -> currentState.maxAmount
            else -> 0f
        }
        when (val currentState = mutableState.value) {
            is AxisChartState.Idle -> {}
            is AxisChartState.Init -> {
                val items: MutableList<AxisChartCategoryItem> = mutableListOf()
                currentState.chartItems.groupBy { it.category }.forEach { (category, list) ->
                    val color = Color.rgb(
                        Random.Default.nextInt(256),
                        Random.Default.nextInt(256),
                        Random.Default.nextInt(256)
                    )
                    linePaint.color = color

                    val textAlign =
                        when (currentState.chartItems.indexOfFirst { it.category == category } % 3) {
                            0 -> Paint.Align.LEFT
                            1 -> Paint.Align.RIGHT
                            else -> Paint.Align.CENTER
                        }
                    textPaint.textAlign = textAlign

                    val path = Path()
                    list.sortedBy { it.time }.forEachIndexed { index, pieChartItem ->
                        val y =
                            (rect.height() - rect.height() * pieChartItem.amount / maxAmount)
                        if (index == 0) {
                            path.moveTo(rect.left, y)
                            if (list.size == 1)
                                path.lineTo((rect.width()), y)
                        } else
                            path.lineTo((rect.width() * (index) / (list.size - 1)), y)
                    }
                    canvas.drawPath(path, linePaint)
                    canvas.drawTextOnPath(category, path, 0f, textPaint.textSize, textPaint)

                    items.add(
                        AxisChartCategoryItem(
                            category = category,
                            color = color,
                            path = path,
                            textAlign = textAlign
                        )
                    )
                }
                mutableState.value =
                    AxisChartState.DrawComplete(categoryItems = items, maxAmount = maxAmount)
            }
            is AxisChartState.DrawComplete -> {
                currentState.categoryItems.forEach {
                    linePaint.color = it.color

                    textPaint.textAlign = it.textAlign

                    canvas.drawPath(it.path, linePaint)
                    canvas.drawTextOnPath(it.category, it.path, 0f, textPaint.textSize, textPaint)
                }
            }
        }

        linePaint.color = Color.BLACK
        canvas.drawLine(0f, rect.height(), measuredWidth.toFloat(), rect.height(), linePaint)
        canvas.drawLine(
            measuredWidth.toFloat(),
            0f,
            measuredWidth.toFloat(),
            rect.height(),
            linePaint
        )

        textPaint.textAlign = Paint.Align.RIGHT
        for (i in 0..maxAmount.toInt() step 500) {
            canvas.drawText(
                i.toString(),
                measuredWidth.toFloat() - DEFAULT_OFFSET,
                rect.height() - rect.height() * i / maxAmount - DEFAULT_OFFSET,
                textPaint
            )
        }
    }
}