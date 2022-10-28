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

    init {
        mPaint.color = Color.BLUE
        mPaint.strokeWidth = 10f
    }

    @JvmName("setData1")
    fun setData(list: List<PiePiece>?) {
        data = list
        invalidate()
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
        val width = 400f
        val height = 240f
        val radius = 100f

        val path = Path()
        path.addCircle(width, height, radius, Path.Direction.CW)
        val paint = Paint()
        paint.color = Color.BLACK
        paint.isAntiAlias = true

        val center_x = 240f
        var center_y = 220f

        val oval = RectF()
        var offset = 30f
        oval[center_x - 200f, center_y - 200f, center_x + 200f] = center_y + 200f
        data?.forEach {
            if (it.isClicked) {

                oval[center_x - 220f, center_y - 220f, center_x + 220f] = center_y + 220f
            } else {

                oval[center_x - 200f, center_y - 200f, center_x + 200f] = center_y + 200f
            }
            canvas?.drawArc(oval, it.start, it.end, true, it.paint)
            newCanvas?.drawArc(oval, it.start, it.end, true, it.paint)

            paint.strokeWidth = 20f
            paint.textSize = 16f
            canvas?.drawLine(480f,0f + offset, 500f, 0f + offset, it.paint)
            canvas?.drawText(it.category, 520f, 0f + offset, paint)
            offset += 30f
        }
        paint.color = Color.WHITE
        canvas?.drawCircle(center_x, center_y, radius + 60f, paint) // рисуем круг
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