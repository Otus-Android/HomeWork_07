package otus.homework.customview.chart_by_day

import android.content.Context
import android.graphics.*
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import otus.homework.customview.ext.dPToPx
import otus.homework.customview.pie_chart.Category
import java.util.*
import kotlin.math.ceil

class DayPaymentChartView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {

    private var dayPayments : List<DayCategoryPayment>? = null
    private var defaultWidth = 40
        get() = field.dPToPx()
    private var defaultHeight = 40
        get() = field.dPToPx()
    private var title : DrawText? = null
    private var startChartPosition : Pair<Float, Float>? = null
    private var maxPaymentSize: Int? = null
    private var maxDaysSize: Int? = null
    private var axisLines: Pair<Path, Paint>? = null
    private var yTexts : List<DrawText>? = null
    private var xTexts : List<DrawText>? = null
    private var paymentsPoints : List<PaymentPoint>? = null
    private var linePathPayment : Pair<Path, Paint>? = null


    fun setCategory(category: Category) {
        val items = mutableListOf<DayCategoryPayment>()
        val days = category.payments.map { it.time.getDayOfYear() }.distinct().sorted()
        days.forEachIndexed { index, dayOfYear ->
            val payments = category.payments.filter { it.time.getDayOfYear() == dayOfYear }
            val time = payments.first().time
            items.add(DayCategoryPayment(index, payments, time.getDayAndMonth()))
        }
        Log.d(this::class.simpleName, "items sum  = ${items.first().sum}")
        dayPayments = items
        fillTitle()
        fillAxes()
        fillChart()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        var widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        var heightSize = MeasureSpec.getSize(heightMeasureSpec)

        if (widthSize < defaultWidth) widthSize = defaultWidth
        if (heightSize < defaultHeight) heightSize = defaultHeight

        when {
            widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST ->
                setMeasuredDimension(defaultWidth, defaultHeight)
            widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.AT_MOST ->
                setMeasuredDimension(widthSize, widthSize)
            widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.EXACTLY ->
                setMeasuredDimension(heightSize, heightSize)
            widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY && widthSize != heightSize -> {
                if (widthSize >= heightMode) setMeasuredDimension(heightSize, heightSize)
                else setMeasuredDimension(widthSize, widthSize)
            }
            (widthMode == MeasureSpec.EXACTLY || widthMode == MeasureSpec.AT_MOST) && heightMode == MeasureSpec.UNSPECIFIED -> {
                setMeasuredDimension(widthSize, widthSize)
            }
            else -> super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState: Parcelable? = super.onSaveInstanceState()
        superState?.let {
            val state = DayCategoryPaymentSavedState(superState)
            state.items = dayPayments
            return state
        } ?: run {
            return superState
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        when (state) {
            is DayCategoryPaymentSavedState -> {
                super.onRestoreInstanceState(state.superState)
                dayPayments = state.items

                requestLayout()
            }
            else -> {
                super.onRestoreInstanceState(state)
            }
        }
    }

    override fun onDraw(canvas: Canvas?) {
        if (canvas == null) return
        title?.let { canvas.drawText(it.text, it.x, it.y, it.paint) }
        axisLines?.let { canvas.drawPath(it.first, it.second) }
        yTexts?.forEach { canvas.drawText(it.text, it.x, it.y, it.paint) }
        xTexts?.forEach { canvas.drawText(it.text, it.x, it.y, it.paint) }
        paymentsPoints?.forEach { canvas.drawCircle(it.x, it.y, it.radius, it.paint) }
        linePathPayment?.let { canvas.drawPath(it.first, it.second) }
    }

    private fun fillTitle() {
        val text = dayPayments?.first()?.payments?.first()?.category ?: ""
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.textAlign = Paint.Align.LEFT
        val textBound = Rect()
        paint.color = Color.BLACK
        paint.textSize= 60f
        paint.style = Paint.Style.STROKE
        paint.getTextBounds(text, 0, text.length, textBound)
        startChartPosition = Pair(20f, -textBound.top.toFloat() + 10f)
        title = DrawText(text, startChartPosition!!.first, startChartPosition!!.second, paint)
    }

    private fun fillYText(value: Int, x : Float, y : Float) : DrawText {
        val text = value.toString()
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.textAlign = Paint.Align.RIGHT
        paint.color = Color.BLACK
        paint.textSize= 20f
        paint.style = Paint.Style.STROKE
        val textBound = Rect()
        paint.getTextBounds(text, 0, text.length, textBound)
        return DrawText(text, x - 5f, y - textBound.top.toFloat() + 5f, paint)
    }

    private fun fillXText(text: String, x : Float, y : Float) : DrawText {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.textAlign = Paint.Align.RIGHT
        paint.color = Color.BLACK
        paint.textSize= 20f
        paint.style = Paint.Style.STROKE
        return DrawText(text, x - 5f, y - 5f, paint)
    }

    private fun fillAxes() {
        val payments = checkNotNull(dayPayments) { return }
        val maxSum = checkNotNull(payments.map { it.sum }.maxOrNull()) { return }
        maxPaymentSize = ceil(((maxSum.toFloat() * 1.2f ) / 1000f)).toInt() * 1000
        val linesPath = Path()
        val startY = checkNotNull(startChartPosition?.second?.plus(20f) ) { return }

        val xTextItems = mutableListOf<DrawText>()
        val yTextItems = mutableListOf<DrawText>()

        List(6) { it * 0.2f }.forEach { step ->
            val xLineTo = width.toFloat()
            val yLineTo = startY + step * (height.toFloat() - startY)
            linesPath.moveTo(0f, yLineTo)
            linesPath.lineTo(xLineTo, yLineTo)
            if (step != 1f) yTextItems.add(fillYText(ceil(((1f - step) * maxPaymentSize!!)).toInt(), xLineTo, yLineTo))
        }

        maxDaysSize = ceil(payments.size.toFloat() * 1.5f).toInt()
        List(maxDaysSize!!) {it}.forEach { day ->
            val xLineTo = (day.toFloat() / maxDaysSize!!.toFloat()) * width.toFloat()
            val yLineTo = height.toFloat()
            linesPath.moveTo(xLineTo, startY)
            linesPath.lineTo(xLineTo, yLineTo)
            if (day != 0 && day <= payments.lastIndex + 1) xTextItems.add(fillXText(payments[day - 1].dayAndMonth, xLineTo, yLineTo))
        }

        xTexts = xTextItems
        yTexts = yTextItems

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.GRAY
        paint.style = Paint.Style.STROKE
        paint.pathEffect = DashPathEffect(floatArrayOf(20f, 5f), 0f)
        axisLines = Pair(linesPath, paint)
    }

    private fun fillChart() {
        val payments = checkNotNull(dayPayments) { return }
        val startY = checkNotNull(startChartPosition?.second?.plus(20f) ) { return }
        val paymentCircleItems = mutableListOf<PaymentPoint>()
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.RED
        paint.style = Paint.Style.FILL

        payments.map { it.sum }.forEachIndexed { index, paymentValue ->
            val y = startY + (1 - paymentValue.toFloat() / maxPaymentSize!!.toFloat()) * (height.toFloat() - startY)
            val x = ((index + 1).toFloat() / maxDaysSize!!.toFloat()) * width.toFloat()
            paymentCircleItems.add(PaymentPoint(x, y, 10f, paint))
        }

        paymentsPoints = paymentCircleItems
        Log.d(this::class.simpleName, "paymentCircleItems.size = ${paymentCircleItems.size}")
        if (paymentCircleItems.size < 1) return

        val curvePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        curvePaint.color = Color.RED
        curvePaint.style = Paint.Style.STROKE
        curvePaint.strokeWidth = 5f
        val curvePath = Path()


        paymentCircleItems.forEachIndexed { index, item ->
            when (index) {
                0 -> curvePath.moveTo(item.x, item.y)
                else -> curvePath.lineTo(item.x, item.y)
            }
        }
        linePathPayment = Pair(curvePath, curvePaint)
    }

    private fun Long.getDayOfYear() : Int {
        val calc = Calendar.getInstance()
        calc.time = Date(this * 1000)
        return calc.get(Calendar.DAY_OF_YEAR)
    }

    private fun Long.getDayAndMonth() : String {
        val calc = Calendar.getInstance()
        calc.time = Date(this * 1000)
        val day = calc.get(Calendar.DAY_OF_MONTH)
        val month = calc.get(Calendar.MONTH) + 1
        return "${day.dateInString()}.${month.dateInString()}"
    }

    private fun Int.dateInString() : String {
        return if (this.toString().length == 1) "0$this"
        else this.toString()
    }

    private data class DrawText(val text : String, val x: Float, val y: Float, val paint: Paint)

    private data class PaymentPoint(val x: Float, val y: Float, val radius : Float, val paint: Paint)
}