package otus.homework.customview

import android.content.Context
import android.graphics.*
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.*

class Grapf(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private var maxAmount: Spending? = null
    private var minAmount: Spending? = null
    private var maxTimeEnd: Date? = null
    private var minTimeStart: Date? = null
    private var btwMinMax: Float? = null
    var grapfData = mapOf<String, List<GrapfPoint>>()
    var gragfTime = mutableListOf<Pair<Pair<Date, Date>, GrapfDate>>()
    private var defaultWidth = 150
    private var widhtPerView = 0f
    var pointWidht = 0f
    val rectDate: RectF = RectF()
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

    var heightMy = 0f
    var heightText = 0f
    var heightDate = 0f
    var bottomDate = 0f
    var sum = 0f
    var radius: Float = 0f
    var innerRadius: Float = 0f
    var midWidth: Float = 0f
    var midHeight: Float = 0f
    val radius1 = 10.0f;
    val mCornerPathEffect = CornerPathEffect(radius1);
    var textSize = 0f
    private val paint: Paint = Paint().apply {
        color = Color.GREEN
        strokeWidth = 10f
        style = Paint.Style.STROKE
        flags = Paint.ANTI_ALIAS_FLAG
    }

    private val paintForRectTextDate: Paint = Paint().apply {
        color = Color.BLACK
        textSize = 1f
        textAlign = Paint.Align.CENTER
        flags = Paint.ANTI_ALIAS_FLAG
    }
    private val paintForRectTextAmount: Paint = Paint().apply {
        color = Color.BLACK
        textSize = 50f
        textAlign = Paint.Align.CENTER
        flags = Paint.ANTI_ALIAS_FLAG
    }
    private val paintForMinMax: Paint = Paint().apply {
        color = Color.BLACK
        textSize = 50f
        flags = Paint.ANTI_ALIAS_FLAG
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val widthMeasureSpec1 = MeasureSpec.getSize(widthMeasureSpec)
        when (MeasureSpec.getMode(widthMeasureSpec)) {
            MeasureSpec.UNSPECIFIED -> {
                setMeasuredDimension(
                    if (grapfData.size == 0) 0 else defaultWidth * (grapfData.size + 4),
                    heightSize
                )
                widhtPerView = if (grapfData.size == 0) 0f else defaultWidth.toFloat()
            }
            MeasureSpec.EXACTLY,
            MeasureSpec.AT_MOST -> {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            }
        }

    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        pointWidht = width * 0.85f / (btwMinMax!!.toFloat())
        // очистка path
        heightMy = height * 0.90f
        heightDate = height * 0.93f
        heightText = height * 0.97f
        bottomDate = height * 0.99f
        radius = ((right - left) / 2).toFloat()

        innerRadius = radius / 2 - 10

        midWidth = ((right - left) / 2).toFloat()
        midHeight = ((bottom - top) / 2).toFloat()


        grapfData.forEach { (name, listPoint) ->
            listPoint.forEach {
                it.y = heightMy - (heightMy / 125) * it.prosent * 100
                it.x =
                    width * 0.03f + pointWidht * (it.date.getDateTime().time - minTimeStart!!.time) / (60 * 1000)
            }
        }
        gragfTime.forEach {
            it.second.firstX =
                width * 0.03f + pointWidht * (it.first.first.time - minTimeStart!!.time) / (60 * 1000)
            it.second.lastX =
                width * 0.03f + pointWidht * (it.first.second.time - minTimeStart!!.time) / (60 * 1000)
        }
        paintForRectTextDate.textSize = 50f
        paintForRectTextAmount.textSize = 50f
        super.onLayout(changed, left, top, right, bottom)
    }


    private var pathLine = Path()
    private var pathCircle = Path()
    private var pathLineForY = Path()

    private fun Canvas.setPoint() {
        var indexForMap = 0
        grapfData.forEach { (name, listPoint) ->
            listPoint.forEachIndexed { index, grapfPoint ->
                if (index == 0)
                    pathLine.moveTo(width * 0.01f, grapfPoint.y)
                pathCircle.addCircle(
                    grapfPoint.x, grapfPoint.y,
                    10f,
                    Path.Direction.CW
                )
                pathLine.lineTo(grapfPoint.x, grapfPoint.y);

            }
            paint.color = colorsList[indexForMap % colorsList.size]
            drawPath(pathLine, paint)
            drawPath(pathCircle, paint)
            pathCircle.reset()
            pathLine.reset()
            pathLine.moveTo(0f, heightMy)
            indexForMap += 1
        }
    }

    private fun Canvas.setPeriod() {
        gragfTime.forEach {
            pathLine.moveTo(it.second.firstX, heightText)
            pathLine.lineTo(it.second.lastX, heightText)
            drawTextOnPath(
                getDateTimeDay(it.first.first.time),
                pathLine,
                0f,
                0f,
                paintForRectTextDate
            )
            rectDate.set(it.second.firstX, heightDate, it.second.lastX, bottomDate)
            drawRoundRect(rectDate, 40f, 40f, paint)
            pathLine.reset()
        }
    }

    private fun Canvas.setMinMaxAmount() {
        pathLineForY.reset()
        paintForMinMax.textSize = resources.getDimension(R.dimen.gant_period_name_text_size)
        var textWidth = paintForMinMax.measureText(maxAmount!!.amount.toString())
        pathLineForY.moveTo(
            (width - textWidth - width * 0.02).toFloat(),
            (heightMy - (heightMy / 125) * 100)
        )
        pathLineForY.lineTo(
            (width).toFloat(),
            (heightMy - (heightMy / 125) * 100)
        );
        drawTextOnPath(
            maxAmount!!.amount.toString(), pathLineForY, 0f,
            0f,
            paintForMinMax
        )
//min
        pathLineForY.reset()

        textWidth = paintForMinMax.measureText(minAmount!!.amount.toString())
        pathLineForY.moveTo((width - textWidth - width * 0.02).toFloat(), (heightMy))
        pathLineForY.lineTo(
            (width).toFloat(),
            (heightMy)
        );
        drawTextOnPath(
            minAmount!!.amount.toString(), pathLineForY, 0f,
            0f,
            paintForMinMax
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        pathLine.reset();
        paint.pathEffect = mCornerPathEffect
        canvas.setPoint()
        canvas.setPeriod()
        canvas.setMinMaxAmount()

    }

    private fun Paint.getTextBaselineByCenter(center: Float) = center - (descent() + ascent()) / 2


    private fun Float.getDateTime(): Date {
        return Date(this.toLong() * 1000)
    }

    private fun getDateTimeDay(s: Long): String {
        try {
            val sdf = SimpleDateFormat("dd MMM yyy")
            val netDate = Date(s)
            return sdf.format(netDate)
        } catch (e: Exception) {
            return e.toString()
        }
    }


    fun atEndOfDay(date: Date?): Date {
        val localDateTime = dateToLocalDateTime(date!!)
        val endOfDay = localDateTime.with(LocalTime.MAX)
        return localDateTimeToDate(endOfDay)
    }

    private fun atStartOfDay(date: Date): Date {
        val localDateTime: LocalDateTime = dateToLocalDateTime(date)
        val startOfDay = localDateTime.with(LocalTime.MIN)
        return localDateTimeToDate(startOfDay)
    }

    private fun dateToLocalDateTime(date: Date): LocalDateTime {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault())
    }

    private fun localDateTimeToDate(localDateTime: LocalDateTime): Date {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant())
    }

    //  name: String, val valuePoint: Float, val date: Float, var prosent: Float
    fun setValues(items: List<Spending>) {
        maxAmount = items.maxByOrNull { it.amount }
        minAmount = items.minByOrNull { it.amount }
        maxTimeEnd = atEndOfDay(items.maxByOrNull { it.time }!!.time.getDateTime())
        val minTimeFloat = items.minByOrNull { it.time }!!.time

        minTimeStart = atStartOfDay(minTimeFloat.getDateTime())
        btwMinMax = (maxTimeEnd!!.time - minTimeStart!!.time) / (60 * 1000f)
        var range = btwMinMax!! / (60 * 24f)
        if ((range - 2f) > 1f) {
            gragfTime.add(Pair(Pair(minTimeStart!!, atEndOfDay(minTimeStart)), GrapfDate(0f, 0f)))
            var dateNext = Date(minTimeStart!!.time + 1000 * 60 * 60 * 24)
            gragfTime.add(
                Pair(
                    Pair(atStartOfDay(dateNext), atEndOfDay(dateNext)),
                    GrapfDate(0f, 0f)
                )
            )
            for (i in 0..(range - 2f).toInt()) {
                dateNext = Date(dateNext.time + 1000 * 60 * 60 * 24)
                gragfTime.add(
                    Pair(
                        Pair(atStartOfDay(dateNext), atEndOfDay(dateNext)),
                        GrapfDate(0f, 0f)
                    )
                )
            }
            gragfTime.add(Pair(Pair(atStartOfDay(maxTimeEnd!!), maxTimeEnd!!), GrapfDate(0f, 0f)))
        } else if ((range - 2f) == 1f) {
            gragfTime.add(Pair(Pair(minTimeStart!!, atEndOfDay(minTimeStart)), GrapfDate(0f, 0f)))
            val dateNext = Date(minTimeStart!!.time + 1000 * 60 * 60 * 24)
            gragfTime.add(Pair(Pair(atStartOfDay(dateNext), dateNext), GrapfDate(0f, 0f)))
            gragfTime.add(Pair(Pair(atStartOfDay(maxTimeEnd!!), maxTimeEnd!!), GrapfDate(0f, 0f)))
        } else {
            gragfTime.add(Pair(Pair(minTimeStart!!, atEndOfDay(minTimeStart)), GrapfDate(0f, 0f)))
            gragfTime.add(Pair(Pair(atStartOfDay(maxTimeEnd!!), maxTimeEnd!!), GrapfDate(0f, 0f)))
        }
        grapfData = items.groupBy { it.category }
            .mapValues { value ->
                value.value.map {
                    GrapfPoint(
                        it.name,
                        it.amount.toFloat(),
                        it.time,
                        it.amount.toFloat() / maxAmount!!.amount.toFloat(),
                        0f,
                        0f
                    )
                }
            }
        requestLayout()
        invalidate()
    }

    override fun onSaveInstanceState(): Parcelable? {
        val savedState = super.onSaveInstanceState()
        savedState?.let {
            return GrapfState(it).also { grapfState ->
                grapfState.grapfData = this.grapfData
                grapfState.gragfTime = this.gragfTime
            }
        } ?: return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is GrapfState) {
            super.onRestoreInstanceState(state.superState)
            grapfData = state.grapfData.toMap()
            gragfTime = state.gragfTime.toMutableList()
            requestLayout()
            invalidate()
        } else super.onRestoreInstanceState(state)
    }
}

class GrapfState(parcelable: Parcelable) : View.BaseSavedState(parcelable) {
    var grapfData = mapOf<String, List<GrapfPoint>>()
    var gragfTime = mutableListOf<Pair<Pair<Date, Date>, GrapfDate>>()
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeMap(grapfData)
        parcel.writeList(gragfTime)
    }

}