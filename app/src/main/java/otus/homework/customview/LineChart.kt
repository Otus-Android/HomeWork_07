package otus.homework.customview

import android.content.Context
import android.graphics.*
import android.icu.text.CompactDecimalFormat
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import java.util.*
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

class LineChart @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {
    private var _maxValue = 0f
    private var _minValue = 0f
    var labels = listOf<String>()
    private var _series = listOf<List<Float>>()
    var series
        get() = _series
        set(value) {
            _series = value

            val minValue = value.map { it.minOrNull() }.minByOrNull { it ?: 0f } ?: 0f
            val maxValue = value.map { it.maxOrNull() }.maxByOrNull { it ?: 0f } ?: 0f
            _verticalSegmentValue = getVerticalSegmentValue(maxValue)
            _maxValue = ceil(maxValue / _verticalSegmentValue) * _verticalSegmentValue
            _minValue = max(0f, floor(minValue / _verticalSegmentValue)) * _verticalSegmentValue
            if (_minValue == _maxValue) {
                _minValue = _maxValue - _maxValue / 10
                _maxValue = _maxValue + _maxValue / 10
            }
            _verticalSegmentsCount = ((_maxValue - _minValue) / _verticalSegmentValue).toInt()
        }

    private var _verticalSegmentValue = 1f
    private var _verticalSegmentsCount = 1

    private val _numberFormat =
        CompactDecimalFormat.getInstance(Locale.US, CompactDecimalFormat.CompactStyle.SHORT)
    private val _leftLabelOffsetY = dpToPx(2f)
    private val _bottomOffset = dpToPx(SPACE_BOTTOM.toFloat()).toFloat()
    private val _labelPaint = Paint()
    private val _horizontalLinePaint = Paint()
    private val _seriePaint = Paint()

    init {
        _labelPaint.flags = Paint.ANTI_ALIAS_FLAG
        _labelPaint.color = ContextCompat.getColor(context, R.color.white50)
        _labelPaint.textSize = dpToPx(9f).toFloat()
        _labelPaint.textAlign = Paint.Align.CENTER

        _horizontalLinePaint.flags = Paint.ANTI_ALIAS_FLAG
        _horizontalLinePaint.style = Paint.Style.STROKE
        _horizontalLinePaint.strokeWidth = dpToPx(1f).toFloat()

        _seriePaint.flags = Paint.ANTI_ALIAS_FLAG
        _seriePaint.style = Paint.Style.STROKE
        _seriePaint.strokeWidth = dpToPx(2f).toFloat()
        _seriePaint.pathEffect = DashPathEffect(floatArrayOf(dpToPx(8f).toFloat(), dpToPx(4f).toFloat()), 0f)

        if (isInEditMode) {
            series = listOf(
                listOf(
                    11f, 6f, 17f, 29f, 21f
                )
            )
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        var width = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        var height = MeasureSpec.getSize(heightMeasureSpec)

        when (widthMode) {
            MeasureSpec.AT_MOST, MeasureSpec.UNSPECIFIED -> if (width > height) {
                width = (series.firstOrNull()?.size ?: 1) * dpToPx(50f)
            }
            MeasureSpec.EXACTLY -> { /* leave exactly width */ }
        }
        when (heightMode) {
            MeasureSpec.AT_MOST, MeasureSpec.UNSPECIFIED -> if (height > width) {
                height = _verticalSegmentsCount * dpToPx(50f)
            }
            MeasureSpec.EXACTLY -> { /* leave exactly height */ }
        }

        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawHorizontalLines(canvas)
        drawBottomLabels(canvas)
        drawSeries(canvas)
    }

    private fun getVerticalSegmentValue(maxValue: Float): Float {
        var verticalSegmentValue = 1f
        if (maxValue > 1) {
            while (maxValue / verticalSegmentValue > 10) {
                verticalSegmentValue *= 10
            }
        } else {
            while (maxValue / verticalSegmentValue <= 1) {
                verticalSegmentValue /= 10
            }
        }
        return verticalSegmentValue
    }

    private fun drawHorizontalLines(canvas: Canvas) {
        val step = (this.height - _bottomOffset - dpToPx(1f + 10)) / _verticalSegmentsCount
        var y = step * _verticalSegmentsCount + dpToPx(10f)
        var value = _minValue
        val lineNumbers = ((_maxValue - _minValue) / _verticalSegmentValue).toInt() + 1
        (0..lineNumbers).forEach {
            drawHorizontalLine(canvas, y, it)
            drawLeftLabel(canvas, y, value)

            value += _verticalSegmentValue
            y -= step
        }
    }

    private fun drawLeftLabel(canvas: Canvas, y: Float, value: Float) {
        val text = _numberFormat.format(value)
        val x = dpToPx(SPACE_LEFT.toFloat() / 3).toFloat()
        canvas.drawText(text, x, y + _leftLabelOffsetY, _labelPaint)
    }

    private fun drawHorizontalLine(canvas: Canvas, y: Float, index: Int) {
        if (index == 0) {
            _horizontalLinePaint.color = ContextCompat.getColor(context, R.color.chartLineColorGray1)
        } else {
            _horizontalLinePaint.color = ContextCompat.getColor(context, R.color.chartLineColorGray2)
        }
        val startX = dpToPx(SPACE_LEFT.toFloat()).toFloat()
        val endX = this.width.toFloat()
        canvas.drawLine(startX, y, endX, y, _horizontalLinePaint)
    }

    private fun drawBottomLabels(canvas: Canvas) {
        val left = dpToPx(SPACE_LEFT.toFloat() * 1.5f).toFloat()
        val width = this.width - left * 1.5f
        val labelsCount = labels.size
        val step = width / (labelsCount - 1)
        val y = this.height.toFloat() - dpToPx(SPACE_BOTTOM.toFloat()) / 2
        var x = left
        (0 until labelsCount).forEach {
            val text = labels[it]
            val textLines = text.split("\n")
            var offsetY = y
            textLines.forEach {
                canvas.drawText(it, x, offsetY, _labelPaint)
                offsetY += _labelPaint.textSize * 1.5f
            }
            x += step
        }
    }

    private fun drawSeries(canvas: Canvas) = series.forEachIndexed { index, list ->
        drawSerie(canvas, list, index)
    }

    private fun drawSerie(canvas: Canvas, serie: List<Float>, index: Int) {
        if (serie.isEmpty()) return

        val left = dpToPx(SPACE_LEFT.toFloat())
        val bottom = dpToPx(SPACE_BOTTOM.toFloat())
        val width = this.width - left
        val height = this.height - bottom

        val path = Path()

        var offsetX = width.toFloat() / (serie.size - 1)
        var x = dpToPx(SPACE_LEFT.toFloat()).toFloat()
        val value = serie[0]
        val yForValue = { value: Float ->
            height.toFloat() - (max(value, _minValue) - _minValue) / (_maxValue - _minValue) * height.toFloat()
        }
        var y = yForValue(value)
        path.moveTo(x, y)

        serie.subList(1, serie.size).forEach {
            val controlX = x + offsetX / 2
            x += offsetX
            val prevY = y
            y = yForValue(it)
            val (controlY1, controlY2) = if (y > prevY) {
                Pair(prevY, y)
            } else {
                Pair(y, prevY)
            }
            path.cubicTo(
                controlX, controlY1,
                controlX, controlY2,
                x, y
            )
        }

        _seriePaint.color = getColorForIndex(index)
        canvas.drawPath(path, _seriePaint)

        if (index == 1) {
            path.lineTo((width + left).toFloat(), height.toFloat())
            path.lineTo(left.toFloat(), height.toFloat())

            val paintGradient = Paint()
            paintGradient.flags = Paint.ANTI_ALIAS_FLAG
            paintGradient.shader = LinearGradient(
                0f,
                getHeight().toFloat() / 5,
                0f,
                getHeight().toFloat() / 5 * 4,
                ContextCompat.getColor(context, R.color.chartLineColorBlueForGradient),
                ContextCompat.getColor(context, R.color.chartLineColorBlueForGradientAlpha),
                Shader.TileMode.MIRROR
            )

            paintGradient.style = Paint.Style.FILL
            canvas.drawPath(path, paintGradient)
        }
    }

    private fun getColorForIndex(index: Int) = when (index) {
        0 -> ContextCompat.getColor(context, R.color.chartLineColorGreen)
        1 -> ContextCompat.getColor(context, R.color.chartLineColorBlue)
        else -> ContextCompat.getColor(context, R.color.chartLineColorGreen)
    }

    override fun onSaveInstanceState(): Parcelable {
        return SavedState(super.onSaveInstanceState()).apply {
            series = this@LineChart.series
            labels = this@LineChart.labels
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }
        super.onRestoreInstanceState(state.superState)
        labels = state.labels
        series = state.series
    }

    class SavedState: BaseSavedState {
        var labels = listOf<String>()
        var series = listOf<List<Float>>()

        constructor(superState: Parcelable?) : super(superState)
        private constructor(source: Parcel?) : super(source) {

        }

        override fun writeToParcel(out: Parcel?, flags: Int) {
            super.writeToParcel(out, flags)

            out?.writeList(labels)
            out?.writeList(series)
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState {
                return SavedState(parcel)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }

    companion object {
        private const val SPACE_LEFT = 22
        private const val SPACE_BOTTOM = 40
    }
}