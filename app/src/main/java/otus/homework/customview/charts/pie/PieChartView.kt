package otus.homework.customview.charts.pie

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import otus.homework.customview.charts.PayloadEntity
import kotlin.math.sqrt

class PieChartView(context: Context, attributeSet: AttributeSet): View(context, attributeSet) {

    private val oval = RectF()
    private val newPaint: Paint = Paint()
    private val color: Color? = null
    private val colors = HashMap<Int, @ColorInt Int>()

    init {
        colors[0] = Color.BLUE
        colors[1] = Color.RED
        colors[2] = Color.YELLOW
        colors[3] = Color.GREEN
        colors[4] = Color.GRAY
        colors[5] = Color.CYAN
        colors[6] = Color.MAGENTA
    }

    private var payloadSum = 0
    private var payload: List<PiePayloadEntity>? = null
    private var listener: OnPieSliceClickListener? = null

    fun updatePayload(payload: List<PiePayloadEntity>){
        this.payload = payload
        payloadSum = payload.fold(0) { acc, entry -> acc + entry.amount }
        invalidate()
    }

    fun pacMan(){}

    private fun calculateOvalSize(){
        oval.top = 0f
        oval.bottom = layoutParams.height.toFloat()
        oval.left = (width / 2) - (layoutParams.height / 2).toFloat()
        oval.right = (width / 2) + (layoutParams.height / 2).toFloat()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        when (widthMode) {
            MeasureSpec.UNSPECIFIED -> {
                Log.d("PieChartView", "onMeasure UNSPECIFIED")
            }
            MeasureSpec.AT_MOST -> {
                Log.d("PieChartView", "onMeasure AT_MOST")
            }
            MeasureSpec.EXACTLY -> {
                Log.d("PieChartView", "onMeasure EXACTLY")
                //super.onMeasure(widthMeasureSpec, heightMeasureSpec)
                setMeasuredDimension(widthSize, heightSize)
            }
        }

        //calculateOvalSize()

    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        Log.d("PieChartView", "onLayout")
    }

    private var startAngle = 0f

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        var currentAngle = startAngle

        payload?.forEachIndexed { index, payloadEntity ->
            val sweepAngle = calcProportionAngle(payloadEntity.amount, payloadSum)
            newPaint.color = colors[index] ?: Color.GREEN
            canvas?.drawArc(oval, currentAngle, sweepAngle, true, newPaint)
            currentAngle += sweepAngle
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        calculateOvalSize()

    }

    private fun calcProportionAngle(part: Int, sum: Int): Float{
        return (part * 360f)/sum
    }


    fun setOnPieSliceClickListener(listener: OnPieSliceClickListener){
        this.listener = listener
    }

    var scrollStartX = 0f
    var scrollStartY = 0f

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        if (event?.action == MotionEvent.ACTION_DOWN){
            scrollStartX = event.x
            scrollStartY = event.y
            Log.d("ACTION_DOWN", "$scrollStartX   $scrollStartY")
            return true
        }

        if (event?.action == MotionEvent.ACTION_MOVE){
            val dx = event.x - scrollStartX
            val dy = event.y - scrollStartY
            val distance = sqrt((dx * dx + dy * dy).toDouble())

            if((dx + dy) > 0){
                startAngle -= distance.toFloat()/100

            }else{
                startAngle += distance.toFloat()/100

            }

            invalidate()
            Log.d("ACTION_MOVE", "$dx   $dy   $distance")
        }



        return super.onTouchEvent(event)
    }

}