package otus.homework.customview

import android.content.Context
import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*


//@RequiresApi(Build.VERSION_CODES.N)
class CustomView2: View {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val dataPayLoad: Array<PayLoad>
    init {
        val gson = Gson()
        val buffer: String = resources.openRawResource(R.raw.payload).bufferedReader().use { it.readText() }
        dataPayLoad = gson.fromJson(buffer, Array<PayLoad>::class.java)
    }

    private val sdf = SimpleDateFormat("yyyy-MM-dd")
    private var resultDraw = dataPayLoad
        .groupBy { sdf.format(Date(it.time*1000)) }
        .mapValues { it -> it.value.sumBy { it.amount } }
        .toSortedMap()
    private val minDate = LocalDate.parse(resultDraw.minOf { it.key } ?: "2001-01-01")
    private val maxDate = LocalDate.parse(resultDraw.maxOf { it.key } ?: "2001-01-01")
    private val countValues = Period.between(minDate, maxDate).days+1 //resultDraw.count()
    private val maxValues = resultDraw.maxOf { it.value } * 1.1

    //fill empty dates
    init {
        var date: LocalDate = minDate
        do {
            val str = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            if (!resultDraw.containsKey(str)){
                resultDraw.plus(Pair(str,0))
            }
            date = date.plusDays(1)
        } while (date < maxDate)
    }

    private val paintFill = Paint().apply {
        color = Color.rgb(255, 126,126)
        style = Paint.Style.FILL
    }

    private var pathFill =  Path()
    private val paintLine = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        strokeWidth = 10f
        style = Paint.Style.FILL_AND_STROKE
    }
    private val paintAxis = Paint().apply {
        color = Color.GRAY
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }
    private val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
        textSize = 40f
    }
    private val paintTextDate = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
        textSize = 50f
        textAlign = Paint.Align.CENTER
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        when (widthMode) {
            MeasureSpec.UNSPECIFIED,
            MeasureSpec.AT_MOST,
            MeasureSpec.EXACTLY -> {
                Log.d(TAG, "onMeasure EXACTLY")
                setMeasuredDimension(widthSize, heightSize)
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        var wColumn = 0f;
        val margin = 100f
        var count = 0f
        var oldX = 0f
        var oldY = 0f
        pathFill.reset()
        if( countValues > 1  ){
            wColumn = ((width-2*margin) / (countValues-1)).toFloat()
            pathFill.moveTo( margin, height - margin)
        }
        for (data in resultDraw){
            if(count != 0f) {
                canvas.drawLine(
                    oldX, oldY, oldX + wColumn * count,
                    (height * (1 - data.value / maxValues)).toFloat(), paintLine
                )
            }
            oldX = margin+wColumn*count
            oldY = (height*(1-data.value/maxValues)).toFloat()
            //vertical line
            canvas.drawLine( oldX , margin, oldX, height- margin, paintAxis)
            canvas.drawText(data.key.substring(8), oldX, height - margin/2, paintTextDate)
            pathFill.lineTo( oldX, oldY)
            count++
        }
        if(oldX != 0f){
            pathFill.lineTo( oldX, oldY)
            pathFill.lineTo( width - margin, height - margin)
            canvas.drawPath(pathFill, paintFill)
        }

        //horizontal lines and text
        canvas.drawLine( margin, margin, width - margin, margin, paintAxis)
        canvas.drawText(maxValues.toString(), margin+marginView, margin-marginView, paintText)
        canvas.drawLine( margin,
            (height/2).toFloat(), width - margin, (height/2).toFloat(), paintAxis)
        canvas.drawText((maxValues/2).toString(), margin+marginView, (height/2-marginView).toFloat(), paintText)
        canvas.drawLine( margin, height - margin, width - margin, height -margin, paintAxis)
    }

    companion object {
        const val TAG = "CustomView"
        const val marginView = 10f
    }

}

data class PayLoadDraw2(
    val amount: Int,
    val date: String
)

