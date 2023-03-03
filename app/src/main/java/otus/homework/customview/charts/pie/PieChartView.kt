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
import kotlin.math.*

class PieChartView(context: Context, attributeSet: AttributeSet): View(context, attributeSet) {

    private val baseOval = RectF()
    private val selectOval = RectF()
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

        textPaint.color = Color.WHITE
        textPaint.isAntiAlias = true
    }

    private var payloadSum = 0
    private var selectIndex = -1
    private var payload: List<PiePayloadEntity>? = null
    private var listener: OnPieSliceClickListener? = null

    fun updatePayload(payload: List<PiePayloadEntity>){
        this.payload = payload
        payloadSum = payload.fold(0) { acc, entry -> acc + entry.amount }
        invalidate()
    }

    fun pacMan(){}

    private fun calculateOvalSize(){
        val basePieSize = min(width, height) * 0.8f
        val selectPieSize = basePieSize * 1.2f

        val centerX = width / 2
        val centerY = height / 2

        baseOval.top = centerY - (basePieSize / 2 )
        baseOval.left = centerX - (basePieSize / 2 )
        baseOval.bottom = centerY + (basePieSize / 2 )
        baseOval.right = centerX + (basePieSize / 2 )

        selectOval.top = centerY - (selectPieSize / 2 )
        selectOval.left = centerX - (selectPieSize / 2 )
        selectOval.bottom = centerY + (selectPieSize / 2 )
        selectOval.right = centerX + (selectPieSize / 2 )

        textPaint.textSize = basePieSize / 30f
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        when(widthMode) {
            MeasureSpec.AT_MOST -> {
                setMeasuredDimension(widthSize, heightSize)
            }
            MeasureSpec.EXACTLY -> {
                setMeasuredDimension(widthSize, heightSize)
            }
            MeasureSpec.UNSPECIFIED -> {

            }
        }

        when(heightMode) {
            MeasureSpec.AT_MOST -> {
                setMeasuredDimension(widthSize, heightSize)
            }
            MeasureSpec.EXACTLY -> {
                setMeasuredDimension(widthSize, heightSize)
            }
            MeasureSpec.UNSPECIFIED -> {

            }
        }

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

            if (index == selectIndex) {
                canvas?.drawArc(selectOval, currentAngle, sweepAngle, true, newPaint)
            }else{
                canvas?.drawArc(baseOval, currentAngle, sweepAngle, true, newPaint)
            }

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

    private var scrollStartX = 0f
    private var scrollStartY = 0f
    private var touchDownTime = 0L

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
        }*/

        if(event?.action == MotionEvent.ACTION_UP){
            if ((System.currentTimeMillis() - touchDownTime) <= 200){
                //looks like a click

                val clickAngle = findClickAngle(scrollStartX, scrollStartY) + startAngle

                clickAssistance.filter { it.value.inRange(clickAngle) }.forEach {
                    selectIndex = it.key
                    invalidate()
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

        val centerX = width / 2
        val centerY = height / 2


        val basePieSize = min(width, height) * 0.8f
        val distance = sqrt((centerX - touchedX) + (centerY - touchedY))

        if (distance > basePieSize){
            return -1f
        }

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