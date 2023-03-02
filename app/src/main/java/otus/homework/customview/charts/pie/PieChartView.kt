package otus.homework.customview.charts.pie

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import otus.homework.customview.charts.PayloadEntity
import kotlin.math.*

class PieChartView(context: Context, attributeSet: AttributeSet): View(context, attributeSet) {

    private val oval = RectF()
    private val newPaint: Paint = Paint()
    private val textPaint: Paint = Paint()
    private val colors = HashMap<Int, @ColorInt Int>()
    private val clickAssistance = HashMap<Int, ClickRange>()

    init {
        colors[0] = Color.parseColor("#56e2cf")
        colors[1] = Color.parseColor("#56aee2")
        colors[2] = Color.parseColor("#5668e2")
        colors[3] = Color.parseColor("#8a56e2")
        colors[4] = Color.parseColor("#cf56e2")
        colors[5] = Color.parseColor("#e256ae")
        colors[6] = Color.parseColor("#e25668")
        colors[7] = Color.parseColor("#e28956")
        colors[8] = Color.parseColor("#e2cf56")
        colors[9] = Color.parseColor("#aee256")

        textPaint.isAntiAlias = true

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
        textPaint.textSize = height / 30f
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

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        var currentAngle = startAngle

        clickAssistance.clear()
        payload?.forEachIndexed { index, payloadEntity ->
            //draw slice
            val sweepAngle = calcProportionAngle(payloadEntity.amount, payloadSum)
            newPaint.color = colors[index] ?: Color.MAGENTA
            canvas?.drawArc(oval, currentAngle, sweepAngle, true, newPaint)

            clickAssistance[index] = ClickRange(currentAngle, currentAngle + sweepAngle)

            //draw text
            val middleAngle = sweepAngle / 2 + currentAngle

            val textX = (layoutParams.height.toFloat() / 2 - layoutParams.height / 8) *
                    cos(Math.toRadians(middleAngle.toDouble())).toFloat() + width / 2
            val textY = (layoutParams.height.toFloat() / 2 - layoutParams.height / 8) *
                    sin(Math.toRadians(middleAngle.toDouble())).toFloat() + layoutParams.height / 2

            canvas?.drawText(payloadEntity.category, textX, textY, textPaint)

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
    var touchDownTime = 0L

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        if (event?.action == MotionEvent.ACTION_DOWN){
            touchDownTime = System.currentTimeMillis()
            scrollStartX = event.x
            scrollStartY = event.y
            Log.d("ACTION_DOWN", "$scrollStartX   $scrollStartY")
            return true
        }
/*
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
*/
        if(event?.action == MotionEvent.ACTION_UP){
            if ((System.currentTimeMillis() - touchDownTime) <= 200){
                //looks like a click
                val clickAngle = findClickAngle(scrollStartX, scrollStartY)

                clickAssistance.filter { it.value.inRange(clickAngle) }.forEach {
                    payload?.get(it.key)?.let { entity ->
                        listener?.onClick(entity)

                    }
                }

                Log.d("CLICK ANGLE", "$clickAngle")
            }
        }


        return super.onTouchEvent(event)
    }

    private fun findClickAngle(touchedX: Float, touchedY: Float): Float {
        val centerX =  (oval.right - oval.left) / 2 //oval.width() / 2
        val centerY =  (oval.bottom - oval.top) / 2

        //val distance = sqrt((centerX - touchedX) + (centerY - touchedY))

        return (Math.toDegrees(
            atan2(
                (centerY - touchedY).toDouble(),
                (centerX - touchedX).toDouble()
            )
        )).toFloat() + 180
    }


    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        return PieChartSavedState(superState)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state != null) {
            val customState = state as PieChartSavedState
            super.onRestoreInstanceState(customState)
        }
    }

    internal class PieChartSavedState(savedState: Parcelable?): BaseSavedState(savedState)

    internal data class ClickRange(val startAngle: Float, val endAngle: Float){
        fun inRange(angle: Float): Boolean {
            return angle in (startAngle..endAngle)
        }
    }

}