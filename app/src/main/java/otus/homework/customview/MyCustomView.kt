package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlin.math.atan2
import kotlin.random.Random


class MyCustomView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var defaultWidth = 600
    var defaultHeight = 600

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        Log.i("myDebug", "onMeasure")

        val measureSpecWidthMode = MeasureSpec.getMode(widthMeasureSpec)
        val measureSpecWidth = MeasureSpec.getSize(widthMeasureSpec)
        val measureSpecHeightMode = MeasureSpec.getMode(heightMeasureSpec)
        val measureSpecHeight = MeasureSpec.getSize(heightMeasureSpec)

        Log.i("myDebug", "onMeasure: measureSpecWidthMode -> $measureSpecWidthMode")
        Log.i("myDebug", "onMeasure: measureSpecWidth -> $measureSpecWidth")
        Log.i("myDebug", "onMeasure: measureSpecHeightMode -> $measureSpecHeightMode")
        Log.i("myDebug", "onMeasure: measureSpecHeight -> $measureSpecHeight")

        val currentWidth: Int = when (measureSpecWidthMode) {
            MeasureSpec.EXACTLY -> {
                measureSpecWidth
            }
            MeasureSpec.AT_MOST -> {
                measureSpecWidth
            }
            MeasureSpec.UNSPECIFIED -> {
                measureSpecWidth
            }
            else -> throw IllegalStateException("Zopa")
        }

        val currentHeight: Int = when (measureSpecHeightMode) {
            MeasureSpec.EXACTLY -> {
                measureSpecWidth
            }
            MeasureSpec.AT_MOST -> {
                measureSpecWidth
            }
            MeasureSpec.UNSPECIFIED -> {
                measureSpecWidth
            }
            else -> throw IllegalStateException("Zopa")
        }

        setMeasuredDimension(currentWidth, currentHeight)
    }

    val linePaint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
        flags = Paint.ANTI_ALIAS_FLAG
        strokeWidth = 10f
    }

    val rectPaint = Paint().apply {
        color = Color.BLUE
        style = Paint.Style.STROKE
        flags = Paint.ANTI_ALIAS_FLAG
        strokeWidth = 10f
    }

    val paintStroke = 200f
    val paint1 = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        flags = Paint.ANTI_ALIAS_FLAG
        strokeWidth = paintStroke
    }

    val paint2 = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
        flags = Paint.ANTI_ALIAS_FLAG
        strokeWidth = paintStroke
    }

    val paint3 = Paint().apply {
        color = Color.BLUE
        style = Paint.Style.STROKE
        flags = Paint.ANTI_ALIAS_FLAG
        strokeWidth = paintStroke
    }
    val path1 = Path()
    val path2 = Path()
    val path3 = Path()
    val path4 = Path()
    val rect1 = RectF()

    val pathMeasure = PathMeasure()

    var centerX = 0f
    var centerY = 0f

    private val bigRadius 
        get() = measuredWidth.toFloat() / 2 - paintStroke / 2

    private val smallRadius
        get() = measuredWidth.toFloat() / 2 - paintStroke - paintStroke / 2

    private val xOffset
        get() = measuredWidth.toFloat() / 2

    private val yOffset
        get() = measuredHeight.toFloat() / 2

    override fun onDraw(canvas: Canvas) {
        canvas.drawRGB(255, 0, 0)

        rect1.set(
            0f + paintStroke,
            0f + paintStroke,
            measuredWidth.toFloat() - paintStroke,
            measuredHeight.toFloat() - paintStroke
        )
        canvas.drawRect(rect1, rectPaint)

        centerX = rect1.centerX()
        centerY = rect1.centerY()

        canvas.drawLine(
            centerX,
            centerY,
            centerX,
            centerY - bigRadius,
            linePaint
        )

        canvas.drawLine(
            centerX,
            centerY,
            centerX + smallRadius,
            centerY,
            linePaint
        )

        path1.addArc(rect1, 0f, 70f)
        path2.addArc(rect1, 75f, 70f)
        path3.addArc(rect1, 150f, 100f)

        canvas.drawPath(path1, paint1)
        canvas.drawPath(path2, paint2)
        canvas.drawPath(path3, paint3)
    }

    private fun isTouchInSegment(
        xPoint: Float,
        yPoint: Float,
        bigRadius: Float,
        smallRadius: Float,
        xOffset: Float,
        yOffset: Float

    ): Boolean {
        val dX = xPoint - xOffset
        val dY = yPoint - yOffset
        Log.i("myDebug", "inCorrectRange: dX -> $dX")
        Log.i("myDebug", "inCorrectRange: dY -> $dY")

        return (dX * dX + dY * dY) < bigRadius * bigRadius &&
                (dX * dX + dY * dY) > smallRadius * smallRadius
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

    private fun Paint.setRandomRGB() {
        this.setARGB(
            255,
            Random.nextInt(256),
            Random.nextInt(256),
            Random.nextInt(256)
        )
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                Log.i("myDebug", "onTouchEvent: x -> ${event.x}")
                Log.i("myDebug", "onTouchEvent: y -> ${event.y}")
                val inCorrectPlaceTouch = isTouchInSegment(
                    xPoint = event.x,
                    yPoint = event.y,
                    bigRadius = bigRadius,
                    smallRadius = smallRadius,
                    xOffset = xOffset,
                    yOffset = yOffset
                )

                val touchPointAngel = getAngleFromTouch(event.x, event.y, xOffset, yOffset)

                when {
                    touchPointAngel in 0.0..70.0 && inCorrectPlaceTouch -> {
                        paint1.setRandomRGB()
                        invalidate()
                    }
                    touchPointAngel in 75.0..145.0 && inCorrectPlaceTouch -> {
                        paint2.setRandomRGB()
                        invalidate()
                    }
                    touchPointAngel in 150.0..250.0 && inCorrectPlaceTouch-> {
                        paint3.setRandomRGB()
                        invalidate()
                    }
                }
            }
        }
        return true
    }
}