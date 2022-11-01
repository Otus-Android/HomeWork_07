package otus.homework.customview.customView

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import otus.homework.customview.PieChartClickListener
import otus.homework.customview.models.PiePiece


class PieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var pieChartClickListener: PieChartClickListener? = null
    private var mPaint: Paint = Paint()
    private var bitmap: Bitmap? = null
    private var data: List<PiePiece>? = emptyList()
    var oval = RectF()
    var bigOval = RectF()
    var center_x = 0f
    var center_y = 0f
    var offset = 0f
    var bOffset = 0f

    init {
        mPaint.color = Color.BLUE
        mPaint.strokeWidth = 10f
    }

    @JvmName("setData1")
    fun setData(list: List<PiePiece>?) {
        data = list
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        Log.d("DDDDDD", layoutParams.width.toString())
        Log.d("DDDDDD", layoutParams.height.toString())
        center_x = layoutParams.height.toFloat() / 2
        center_y = layoutParams.height.toFloat() / 2
        offset = layoutParams.height.toFloat() / 2
        bOffset = (layoutParams.height.toFloat() / 2) - 20f

        Log.d("DDDDDD",center_x.toString())
        Log.d("DDDDDD", center_y.toString())
        Log.d("DDDDDD", offset.toString())
        Log.d("DDDDDD", bOffset.toString())
        oval[center_x - offset, center_y - offset, center_x + offset] = center_y + offset
        bigOval[center_x - bOffset, center_y - bOffset, center_x + bOffset] = center_y + bOffset
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        when (MeasureSpec.getMode(widthMeasureSpec)) {
            MeasureSpec.UNSPECIFIED,
            MeasureSpec.AT_MOST,
            MeasureSpec.EXACTLY -> {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            }
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val newCanvas = bitmap?.let { Canvas(it) }

        val path = Path()
//        path.addCircle(width, height, radius, Path.Direction.CW)
        val paint = Paint()
        paint.color = Color.BLACK
        paint.isAntiAlias = true
        data?.forEach {
            if (it.isClicked) {
                canvas?.drawArc(oval, it.start, it.end, true, it.paint)
                newCanvas?.drawArc(oval, it.start, it.end, true, it.paint)
            } else {
                canvas?.drawArc(bigOval, it.start, it.end, true, it.paint)
                newCanvas?.drawArc(bigOval, it.start, it.end, true, it.paint)
            }

            paint.strokeWidth = 20f
            paint.textSize = 16f
        }
        paint.color = Color.WHITE
        canvas?.drawCircle(center_x, center_y, bOffset-40f, paint) // рисуем круг
        paint.color = Color.BLACK
        paint.textSize = 30f
        paint.textAlign = Paint.Align.CENTER
        val text = data?.find { it.isClicked }?.category
        canvas?.drawText(text ?: "", center_x, center_y, paint)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val color = event?.let { bitmap?.getPixel(event.x.toInt(), event.y.toInt()) }
        if (event?.action == MotionEvent.ACTION_DOWN) return true
        if (event?.action == MotionEvent.ACTION_UP) {
            data?.forEach {
                if (it.paint.color == color) {
                    pieChartClickListener?.onClick(it.category)
                }
            }
        }
        return super.onTouchEvent(event)
    }
}