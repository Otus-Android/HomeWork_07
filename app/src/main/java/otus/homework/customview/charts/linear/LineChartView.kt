package otus.homework.customview.charts.linear

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.annotation.ColorInt
import otus.homework.customview.charts.pie.PiePayloadEntity
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.cos
import kotlin.math.sin

class LineChartView(context: Context, attributeSet: AttributeSet): View(context, attributeSet) {

    private val axisPaint: Paint = Paint()
    private val guideAxisPaint: Paint = Paint()
    private val linePaint: Paint = Paint()
    private val textPaint: Paint = Paint()
    private val colors = HashMap<Int, @ColorInt Int>()
    private val dateFormat = SimpleDateFormat("dd.MM", Locale.ROOT)

    init {
        colors[0] = Color.parseColor("#56e2cf")
        colors[1] = Color.parseColor("#56aee2")
        colors[2] = Color.parseColor("#805668e2")
        colors[3] = Color.parseColor("#8a56e2")
        colors[4] = Color.parseColor("#cf56e2")
        colors[5] = Color.parseColor("#e256ae")
        colors[6] = Color.parseColor("#e25668")
        colors[7] = Color.parseColor("#e28956")
        colors[8] = Color.parseColor("#e2cf56")
        colors[9] = Color.parseColor("#aee256")

        axisPaint.color = colors[0]!!
        axisPaint.strokeWidth = 8f

        guideAxisPaint.color = colors[2]!!
        guideAxisPaint.strokeWidth = 2f

        textPaint.color = Color.WHITE
        textPaint.isAntiAlias = true

        linePaint.color = colors[4]!!
        linePaint.strokeWidth = 4f

    }

    private var payload: List<LinePayloadEntry>? = null
    fun updatePayload(payload: List<LinePayloadEntry>){
        this.payload = payload
        Log.d("LINE PAYLOAD", payload.size.toString())
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
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

        //calculateOvalSize()

    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        Log.d("LineChartView", "onLayout")
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        textPaint.textSize = h / 30f
    }

    private val mainAxisHorizontalPadding = 64f
    private val mainAxisVerticalPadding = 64f

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        drawAxis(canvas)
        drawGuides(canvas)
        drawDotsAndLines(canvas)

    }


    private fun drawAxis(canvas: Canvas?){
        //main axis
        canvas?.drawLine(mainAxisVerticalPadding, height.toFloat() - mainAxisVerticalPadding, width.toFloat() - mainAxisVerticalPadding, height.toFloat() - mainAxisVerticalPadding, axisPaint)
        canvas?.drawLine(mainAxisHorizontalPadding, height.toFloat() - mainAxisHorizontalPadding, mainAxisHorizontalPadding, mainAxisHorizontalPadding, axisPaint)

        //guide axis


    }

    private fun drawGuides(canvas: Canvas?){

        val chunkLengthHorizontal = (width - (mainAxisHorizontalPadding * 4)) / 4
        val startX = mainAxisHorizontalPadding * 2
        for (i in 0 until 5){
            canvas?.drawLine(
                startX + (chunkLengthHorizontal * i),
                height.toFloat() - mainAxisHorizontalPadding,
                startX + (chunkLengthHorizontal * i),
                mainAxisHorizontalPadding,
                guideAxisPaint
            )

            payload?.getOrNull(i)?.let {
                val textX = (startX + (chunkLengthHorizontal * i)) - mainAxisHorizontalPadding / 2
                val textY = height.toFloat() - (mainAxisHorizontalPadding / 2)
                canvas?.drawText(dateFormat.format(Date(it.date * 1000L)), textX, textY, textPaint)
            }

        }


        val payloadMax = (payload?.maxOf { it.amount } ?: 0).toFloat()
        val chunkLengthVertical = (height - (mainAxisVerticalPadding * 4)) / 4

        val startY = mainAxisHorizontalPadding
        for (i in 0 until 5){
            canvas?.drawLine(
                width.toFloat() - mainAxisHorizontalPadding,
                startY + (chunkLengthVertical * i),
                mainAxisHorizontalPadding,
                startY + (chunkLengthVertical * i),
                guideAxisPaint
            )

            val textX = width.toFloat() - mainAxisHorizontalPadding
            val textY = startY + (chunkLengthVertical * i)



            canvas?.drawText( (payloadMax - ((payloadMax / 5) * i) ).toString() , textX, textY, textPaint)

        }


    }

    private fun drawDotsAndLines(canvas: Canvas?){

        val startX = mainAxisHorizontalPadding * 2
        val chunkLengthHorizontal = (width - (mainAxisHorizontalPadding * 4)) / 4
        val chunkLengthVertical = (height - (mainAxisVerticalPadding * 4)) / 4

        val payloadMax = payload?.maxOf { it.amount } ?: 0
        val payloadMaxInCords = (height - (mainAxisVerticalPadding * 4))

        Log.d("payloadMax", "$payloadMax")
        Log.d("payloadMaxInCords", "$payloadMaxInCords")

        var prevCircle: Pair<Float, Float>? = null

        payload?.forEachIndexed { index, linePayloadEntry ->

            val circleX = startX + (chunkLengthHorizontal * index)
            val fractionOfMax = linePayloadEntry.amount / payloadMax.toFloat()

            val circleY = payloadMaxInCords - ((payloadMaxInCords * linePayloadEntry.amount)/ payloadMax.toFloat()) + (mainAxisVerticalPadding)

            Log.d("fractionOfMax", "$fractionOfMax")
            Log.d("linePayloadEntry.amount", "${linePayloadEntry.amount}")

            Log.d("circleY", "$circleY")

            //val circleY = height.toFloat() / 2

            canvas?.drawCircle(circleX, circleY, 5f, textPaint)

            prevCircle?.let {
                canvas?.drawLine(
                    it.first,
                    it.second,
                    circleX,
                    circleY,
                    linePaint
                )
            }

            prevCircle = Pair(circleX, circleY)


        }

    }

}