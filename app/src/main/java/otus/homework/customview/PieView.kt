package otus.homework.customview

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.animation.doOnEnd
import androidx.core.graphics.toRegion

class PieView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private val paint: Paint = Paint().apply {
        color = Color.GREEN
        strokeWidth = 10f
        style = Paint.Style.STROKE
        flags = Paint.ANTI_ALIAS_FLAG
    }

    private val colorsList = listOf(
        resources.getColor(R.color.pie_1, null),
        resources.getColor(R.color.pie_2, null),
        resources.getColor(R.color.pie_3, null),
        resources.getColor(R.color.pie_4, null),
        resources.getColor(R.color.pie_5, null),
        resources.getColor(R.color.pie_6, null),
        resources.getColor(R.color.pie_7, null),
        resources.getColor(R.color.pie_8, null),
        resources.getColor(R.color.pie_9, null),
        resources.getColor(R.color.pie_10, null),
    )


    var heightSliceRect = 0f
    var heightSliceRectMod4 = 0f
    var heightSliceRectMod3 = 0f
    var heightSliceRectMod2 = 0f
    var widthSliceRect = 0f
    private var lastXTouch: Float = 0.0f
    private var lastYTouch: Float = 0.0f
    var sum = 0f
    var radius: Float = 0f
    var radiusForAnim: Float = 0f

    var innerRadius: Float = 0f

    var midWidth: Float = 0f
    var midHeight: Float = 0f
    private var region = Region()
    private var chartSliceData = mutableListOf<ChartSlice>()
    private var innerRect = RectF()
    private var sliceRect1 = RectF()
    private var sliceRect2 = RectF()
    private var sliceRect3 = RectF()
    private var isAnimate = false
    private var isDraw = false
    var listener: ClickListener? = null
    var listenerDraw: DrawListener? = null
    val rec: RectF = RectF()
    val recFirst: RectF = RectF()
    val recSecond: RectF = RectF()
    val recThird: RectF = RectF()
    val rec2: RectF = RectF()

    fun setlistener(listener: DrawListener) {
        listenerDraw = listener
    }

    private val paintWhite: Paint = Paint().apply {
        color = Color.WHITE
        textSize = 35f
        textAlign = Paint.Align.CENTER
        style = Paint.Style.FILL
        flags = Paint.ANTI_ALIAS_FLAG
    }

    private val paintGreen: Paint = Paint().apply {
        color = Color.GREEN
        textSize = 40f
        textAlign = Paint.Align.CENTER
        style = Paint.Style.FILL
        flags = Paint.ANTI_ALIAS_FLAG
    }
    private val list = ArrayList<Int>()
    private var maxValue = 0
    private var defaultWidth = 250
    var widhtPerView = 0

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        when (widthMode) {
            MeasureSpec.UNSPECIFIED -> {
                setMeasuredDimension(
                    if (list.size == 0) 0 else defaultWidth * list.size,
                    heightSize
                )
                widhtPerView = if (list.size == 0) 0 else defaultWidth
            }
            MeasureSpec.AT_MOST -> setMeasuredDimension(
                (289 * context.resources.displayMetrics.density).toInt(),
                (349 * context.resources.displayMetrics.density).toInt()
            )
            MeasureSpec.EXACTLY -> {
                Log.d(TAG, "onMeasure EXACTLY")
                super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            }
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        radius = ((right - left) / 2).toFloat()
        radiusForAnim = radius

        innerRadius = radius / 2 - 10

        midWidth = ((right - left) / 2).toFloat()
        midHeight = ((bottom - top) / 2).toFloat()
        rec2.set(
            radius - radius,
            top.toFloat(),
            radius + radius,
            2 * radius
        )
        rec.set(0.toFloat(), 0.toFloat(), right.toFloat(), bottom.toFloat())
        innerRect.set(
            radius - innerRadius,
            radius - innerRadius,
            radius + innerRadius,
            radius + innerRadius
        )

        heightSliceRect = height - 2 * radius
        heightSliceRectMod4 = heightSliceRect / 4
        heightSliceRectMod3 = heightSliceRect / 3
        heightSliceRectMod2 = heightSliceRect / 2
        widthSliceRect = width.toFloat()
        val widthSliceRectMod2 = widthSliceRect / 2
        region = rec.toRegion()


        super.onLayout(changed, left, top, right, bottom)
    }

    var currentAngle: Float = 0f
    var sliceNumber3: ChartSlice? = null
    val pathForTextBox = Path()
    var firstTimeDraw = true

    private fun Canvas.setRectForNameSlice() {
        sliceRect1.set(
            midWidth / 32,
            2 * radius + heightSliceRectMod4 / 4,
            midWidth,
            2 * radius + heightSliceRectMod2
        )
        sliceRect2.set(
            midWidth + midWidth / 32,
            2 * radius + heightSliceRectMod4 / 4,
            2 * midWidth - midWidth / 32,
            2 * radius + heightSliceRectMod2
        )
        sliceRect3.set(
            midWidth / 2,
            2 * radius + heightSliceRectMod2 + midWidth / 32,
            midWidth + midWidth / 2,
            2 * radius + heightSliceRect - midWidth / 32
        )
    }

    private fun Canvas.setChartBody() {
        chartSliceData.forEachIndexed { i, slice ->
            slice.path.reset()
            if (i == 2) {
                sliceNumber3 = slice
                sliceNumber3?.let {
                    recThird.set(
                        radius - innerRadius - ((radiusForAnim / 100) * it.angle2),
                        radius - innerRadius - ((radiusForAnim / 100) * it.angle2),
                        radius + innerRadius + ((radiusForAnim / 100) * it.angle2),
                        radius + innerRadius + ((radiusForAnim / 100) * it.angle2)
                    )
                }
            }
            if (i == 0) {
                recFirst.set(
                    radius - innerRadius - ((radiusForAnim / 100) * slice.angle2),
                    radius - innerRadius - ((radiusForAnim / 100) * slice.angle2),
                    radius + innerRadius + ((radiusForAnim / 100) * slice.angle2),
                    radius + innerRadius + ((radiusForAnim / 100) * slice.angle2)
                )
                paint.color = colorsList[i % colorsList.size]

                paint.style = Paint.Style.FILL
                slice.path.arcTo(recFirst, currentAngle, slice.angle)
                slice.path.arcTo(innerRect, currentAngle + slice.angle, -slice.angle)
                slice.path.close()
                currentAngle += slice.angle + 1
                drawRoundRect(
                    sliceRect1,
                    50f,
                    50f,
                    paint
                )
                pathForTextBox.reset()
                pathForTextBox.moveTo(midWidth / 32, 2 * radius + heightSliceRectMod3)
                pathForTextBox.lineTo(midWidth, 2 * radius + heightSliceRectMod3)
                drawTextOnPath(slice.name, pathForTextBox, 0f, 0f, paintWhite)
            } else {
                if (i > 1) {
                    paint.color = colorsList[i % colorsList.size]

                    paint.style = Paint.Style.FILL
                    slice.path.arcTo(recThird, currentAngle, slice.angle)
                    slice.path.arcTo(innerRect, currentAngle + slice.angle, -slice.angle)
                    slice.path.close()
                    currentAngle += if (chartSliceData.lastIndex - 1 == i) {
                        slice.angle - 3
                    } else {
                        slice.angle
                    }
                }
                if (i == 1) {
                    recSecond.set(
                        radius - innerRadius - ((radiusForAnim / 100) * slice.angle2),
                        radius - innerRadius - ((radiusForAnim / 100) * slice.angle2),
                        radius + innerRadius + ((radiusForAnim / 100) * slice.angle2),
                        radius + innerRadius + ((radiusForAnim / 100) * slice.angle2)
                    )
                    paint.color = colorsList[i % colorsList.size]

                    paint.style = Paint.Style.FILL
                    slice.path.arcTo(recSecond, currentAngle, slice.angle)
                    slice.path.arcTo(innerRect, currentAngle + slice.angle, -slice.angle)
                    slice.path.close()
                    currentAngle += slice.angle + 1
                    drawRoundRect(
                        sliceRect2,
                        50f,
                        50f,
                        paint
                    )
                    pathForTextBox.reset()
                    pathForTextBox.moveTo(
                        midWidth + midWidth / 32,
                        2 * radius + heightSliceRectMod3
                    )
                    pathForTextBox.lineTo(
                        2 * midWidth - midWidth / 32,
                        2 * radius + heightSliceRectMod3
                    )
                    drawTextOnPath(slice.name, pathForTextBox, 0f, 0f, paintWhite)
                }
            }
            drawPath(slice.path, paint)
        }
    }

    private fun Canvas.setRectForAnySpending() {
        drawRoundRect(
            sliceRect3,
            50f,
            50f,
            paintGreen
        )
        // p.reset()
        val pathLine = Path()
        pathLine.moveTo(
            midWidth / 2,
            2 * radius + heightSliceRectMod2 + heightSliceRectMod2 / (1.8).toFloat()
        )
        pathLine.lineTo(
            midWidth + midWidth / 2,
            2 * radius + heightSliceRectMod2 + heightSliceRectMod2 / (1.8).toFloat()
        )

        drawTextOnPath("Остальное", pathLine, 0f, 0f, paintWhite)
        drawPath(pathLine, paint)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        isDraw = false
        canvas.setRectForNameSlice()
        canvas.setChartBody()
        canvas.setRectForAnySpending()

        isDraw = true
        if (firstTimeDraw)
            listenerDraw?.drawDone()
        firstTimeDraw = false
    }

    fun setValues(items: Map<String, Float>) {
        sum = items.values.sum()
        val procent = sum / 100
        var pieSliceData = listOf<ChartSlice>()
        pieSliceData = items.map {
            ChartSlice(it.key, (360 * it.value / sum), (it.value / procent), it.value)
        }
        this.chartSliceData = pieSliceData.toMutableList()
        this.chartSliceData.sortByDescending { it.angle }
        this.chartSliceData.toString()
        requestLayout()
        invalidate()
    }

    fun startAnimation() {

        if (isDraw) {
            isAnimate = true
            ValueAnimator.ofFloat(0.toFloat(), midWidth).apply {
                duration = 500
                repeatCount = 0
                repeatMode = ValueAnimator.RESTART
                addUpdateListener {
                    midWidth = it.animatedValue as Float
                    invalidate()
                }
                start()
            }.doOnEnd {
                isAnimate = false
            }
            isAnimate = true
            ValueAnimator.ofFloat(0.toFloat(), radiusForAnim).apply {
                duration = 500
                repeatCount = 0
                repeatMode = ValueAnimator.RESTART
                addUpdateListener {
                    radiusForAnim = it.animatedValue as Float
                    invalidate()
                }
                start()
            }.doOnEnd {
                isAnimate = false
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isDraw) {
            val r = Region()
            chartSliceData.forEach {
                r.setPath(it.path, region)
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        lastXTouch = event.x
                        lastYTouch = event.y
                    }
                    MotionEvent.ACTION_UP -> {
                        if (r.contains(lastXTouch.toInt(), lastYTouch.toInt())) {
                            listener?.onClick(it.name)
                        }
                    }
                }
            }
        }
//        if (!isAnimate)
//            startAnimation()
        return true
    }

    fun interface ClickListener {
        fun onClick(name: String)
    }

    override fun onSaveInstanceState(): Parcelable? {
        val savedState = super.onSaveInstanceState()
        savedState?.let {
            return ChartState(it).also { chartState ->
                chartState.chartSliceData = this.chartSliceData
            }
        } ?: return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is ChartState) {
            super.onRestoreInstanceState(state.superState)
            chartSliceData = state.chartSliceData.toMutableList()
            requestLayout()
            invalidate()
        } else super.onRestoreInstanceState(state)
    }

    companion object {
        const val TAG = "CustomView"
    }
}

class ChartState(parcelable: Parcelable) : View.BaseSavedState(parcelable) {
    var chartSliceData = listOf<ChartSlice>()
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeList(chartSliceData)
    }
}

interface DrawListener {
    fun drawDone()
}