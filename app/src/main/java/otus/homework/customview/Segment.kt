package otus.homework.customview

import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.os.Parcelable
import androidx.annotation.ColorInt
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Parcelize
class Segment(
    private val startAngel: Float,
    private val endAngel: Float,
    var category: String = "",
    @ColorInt var color: Int = generateRandomColor(),
    var percents: Float = 0f
) : Parcelable {

    @IgnoredOnParcel
    var segmentWidth: Float = 0f
        get() = field * (percents / 100f + 0.4f)

    @IgnoredOnParcel
    private val df = DecimalFormat("#.##").apply {
        roundingMode = RoundingMode.DOWN
    }

    @IgnoredOnParcel
    private val offsetOfPercentTextRect = 3f.dp

    @IgnoredOnParcel
    private val offsetOfPercentText = 24f.dp

    @IgnoredOnParcel
    private val roundRectOfPercentText = RectF()

    @IgnoredOnParcel
    private val valueOfPercentText by lazy { "${df.format(percents)} %" }

    @IgnoredOnParcel
    private val arcPath = Path()

    @IgnoredOnParcel
    private val blurFilter = BlurMaskFilter(3f.dp, BlurMaskFilter.Blur.SOLID)

    @IgnoredOnParcel
    private val textPaint = Paint().apply {
        color = Color.LTGRAY
        style = Paint.Style.FILL
        flags = Paint.ANTI_ALIAS_FLAG
        textSize = 10f.dp
        textAlign = Paint.Align.CENTER
    }

    @IgnoredOnParcel
    private val rectPaint = Paint().apply {
        color = Color.LTGRAY
        style = Paint.Style.STROKE
        flags = Paint.ANTI_ALIAS_FLAG
        strokeWidth = 1f.dp
        maskFilter = blurFilter
    }

    fun onDraw(
        x0: Float,
        y0: Float,
        maxSegmentWidth: Float,
        smallRadius: Float,
        paint: Paint,
        canvas: Canvas
    ) {
        segmentWidth = maxSegmentWidth

        drawSegment(x0, smallRadius, y0, paint, canvas)
        drawPercentText(
            canvas = canvas,
            segmentWidth = segmentWidth,
            smallRadius = smallRadius,
            x0 = x0,
            y0 = y0,
            paint = paint,
            offset = offsetOfPercentText
        )
    }

    private fun drawSegment(
        x0: Float,
        smallRadius: Float,
        y0: Float,
        paint: Paint,
        canvas: Canvas
    ) {
        arcPath.reset()
        arcPath.addArc(
            x0 - smallRadius - segmentWidth / 2,
            y0 - smallRadius - segmentWidth / 2,
            x0 + smallRadius + segmentWidth / 2,
            y0 + smallRadius + segmentWidth / 2,
            startAngel,
            endAngel - startAngel
        )

        paint.color = color
        paint.strokeWidth = segmentWidth
        paint.maskFilter = blurFilter

        canvas.drawPath(arcPath, paint)
    }

    private fun drawPercentText(
        canvas: Canvas,
        segmentWidth: Float,
        smallRadius: Float,
        x0: Float,
        y0: Float,
        paint: Paint,
        offset: Float
    ) {
        val centerOfSegment = startAngel + (endAngel - startAngel) / 2
        val alfa = centerOfSegment * Math.PI / 180

        val x = x0 + (smallRadius + segmentWidth + offset) * cos(alfa).toFloat()
        val y = y0 + (smallRadius + segmentWidth + offset) * sin(alfa).toFloat()

        val text = valueOfPercentText
        val measureText = textPaint.measureText(text)
        val textSize = textPaint.textSize

        val leftRect = x - measureText / 2 - offsetOfPercentTextRect
        val topRect = y - textSize / 2 - offsetOfPercentTextRect
        val rightRect = x + measureText / 2 + offsetOfPercentTextRect
        val bottomRect = y + textSize / 2 + offsetOfPercentTextRect

        roundRectOfPercentText.set(leftRect, topRect, rightRect, bottomRect)

        val fontMetricsInt: Paint.FontMetricsInt = textPaint.fontMetricsInt
        canvas.drawRoundRect(roundRectOfPercentText, 8f, 8f, rectPaint)

        val centerYText: Float =
            roundRectOfPercentText.centerY() - (fontMetricsInt.top + fontMetricsInt.bottom) / 2
        canvas.drawText(text, x, centerYText, textPaint)
    }

    fun isTouchInCurrentSegment(
        xPoint: Float,
        yPoint: Float,
        x0Point: Float,
        y0Point: Float
    ): Boolean {
        val angel = getAngleFromTouch(
            xPoint = xPoint,
            yPoint = yPoint,
            x0Point = x0Point,
            y0Point = y0Point
        )

        return angel in startAngel..endAngel
    }

    fun isTouchInSegmentRange(
        xPoint: Float,
        yPoint: Float,
        smallRadius: Float,
        x0: Float,
        y0: Float
    ): Boolean {
        val dX = xPoint - x0
        val dY = yPoint - y0

        val bigRadius = smallRadius + segmentWidth

        return (dX * dX + dY * dY) < bigRadius * bigRadius
                && (dX * dX + dY * dY) > smallRadius * smallRadius
    }

    private fun getAngleFromTouch(
        xPoint: Float,
        yPoint: Float,
        x0Point: Float,
        y0Point: Float
    ): Float {
        val dX = (x0Point - xPoint).toDouble()
        val dY = (y0Point - yPoint).toDouble()

        val angelInRadians = atan2(dY, dX)
        val angelInDegrees = angelInRadians * 180 / Math.PI

        return angelInDegrees.toFloat() + 180
    }
}
