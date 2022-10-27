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
import otus.homework.customview.models.Metka
import otus.homework.customview.models.PiePiece


class PieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var pieChartClickListener: PieChartClickListener? = null
    private var mPaint: Paint = Paint()
    private var bitmap: Bitmap? = null
    var count: Int = 0
    var delitel: Int = 0
    var data: List<PiePiece>? = emptyList()

    init{
        mPaint.color = Color.BLUE
        mPaint.strokeWidth = 10f
    }

    @JvmName("setData1")
    fun setData(list: List<PiePiece>?) {
        data = list
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
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
        paint.color = Color.BLACK // установим белый цвет

        paint.strokeWidth = 5f
        paint.style = Paint.Style.FILL_AND_STROKE // заливаем

        paint.isAntiAlias = true

        val center_x = 240f
        var center_y = 220f

        val oval = RectF()
        oval[center_x - radius, center_y - radius, center_x + radius] = center_y + radius
//        canvas?.drawArc(oval, 0f, 0f, true, paint) // рисуем пакмана
//        canvas?.drawCircle( center_x, center_y, radius, paint) // рисуем круг


// рисуем большого пакмана без заливки

// рисуем большого пакмана без заливки
//        paint.style = Paint.Style.STROKE
        oval[center_x - 200f, center_y - 200f, center_x + 200f] = center_y + 200f
        data?.forEach {
            canvas?.drawArc(oval, it.start, it.end, true, it.paint)
            newCanvas?.drawArc(oval, it.start, it.end, true, it.paint)
        }
        paint.color = Color.WHITE
        canvas?.drawCircle( center_x, center_y, radius+60f, paint) // рисуем круг


        paint.style = Paint.Style.STROKE

// рисуем разорванное кольцо

// рисуем разорванное кольцо
        center_y = 540f
        oval[center_x - radius, center_y - radius, center_x + radius] = center_y + radius
//        canvas?.drawArc(oval, 135f, 270f, false, paint)
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