package otus.homework.customview

import android.content.Context
import android.graphics.*
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.google.gson.Gson
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.random.Random


//@RequiresApi(Build.VERSION_CODES.N)
class CustomView2 @JvmOverloads constructor (context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0): View(context, attrs, defStyleAttr) {

    private var dataPayLoad: Array<PayLoad>
    private val rndColor = Random

    private val sdf = SimpleDateFormat("yyyy-MM-dd")
    init {
        val gson = Gson()
        val buffer: String = resources.openRawResource(R.raw.payload).bufferedReader().use { it.readText() }
        dataPayLoad = gson.fromJson(buffer, Array<PayLoad>::class.java)

        for (elem in dataPayLoad){
            sdf.format(Date(elem.time*1000)).also { elem.strDate = it }
        }
    }


//    private var resultDraw = dataPayLoad
//        .groupBy { sdf.format(Date(it.time*1000))}
//        //.groupBy { Pair(sdf.format(Date(it.time*1000)), it.category)}
//        .mapValues { it -> it.value.sumBy { it.amount } }
//        .toSortedMap()
//    private val minDate = LocalDate.parse(resultDraw.minOf { it.key } ?: "2001-01-01")
//    private val maxDate = LocalDate.parse(resultDraw.maxOf { it.key } ?: "2001-01-01")
//    private val countValues = Period.between(minDate, maxDate).days+1 //resultDraw.count()
//    private val maxValues = resultDraw.maxOf { it.value } * 1.1

    private var resultDraw2 = dataPayLoad
        .groupingBy { Pair(it.category, it.strDate) }
        .reduce { _, acc, element ->
            acc.copy(amount = acc.amount + element.amount)
        }
        .values.toList()
        .sortedBy { it.category + it.strDate }

    private val minDate = LocalDate.parse(dataPayLoad.minOf { it.strDate } ?: "2001-01-01")
    private val maxDate = LocalDate.parse(dataPayLoad.maxOf { it.strDate } ?: "2001-01-01")
    private val countValues = Period.between(minDate, maxDate).days+1 //resultDraw.count()
    private val maxValues = resultDraw2.maxOf { it.amount }

    val resultDraw3 = resultDraw2.groupBy { it.category }

    //fill empty dates
//    init {
//        var date: LocalDate = minDate
//        do {
//            val str = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
//            if (!resultDraw.containsKey(str)){
//                resultDraw.plus(Pair(str,0))
//            }
//            date = date.plusDays(1)
//        } while (date <= maxDate)
//    }

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

    override fun onSaveInstanceState(): Parcelable? {
        val bundle = Bundle()
        bundle.putParcelable("superState", super.onSaveInstanceState())
        bundle.putParcelableArray("data", this.dataPayLoad)
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) // implicit null check
        {
            val data = state.getParcelableArray("data")
            if (data is Array<*> && data.isArrayOf<PayLoad>()) {
                this.dataPayLoad = data as Array<PayLoad>
            }
        }
        super.onRestoreInstanceState(state)
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        //val heightMode = MeasureSpec.getMode(heightMeasureSpec)
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

    fun DrawHorizontalLinesText(canvas: Canvas){
        //top
        canvas.drawLine( marginPlot, marginPlot, width - marginPlot, marginPlot, paintAxis)
        canvas.drawText(maxValues.toString(), marginPlot+marginView, marginPlot-marginView, paintText)
        //middle
        canvas.drawLine( marginPlot,
            (height/2).toFloat(), width - marginPlot, (height/2).toFloat(), paintAxis)
        canvas.drawText((maxValues/2).toString(), marginPlot+marginView, (height/2-marginView), paintText)
        //down
        canvas.drawLine( marginPlot, height - marginPlot, width - marginPlot, height -marginPlot, paintAxis)
    }
    
    fun DrawVerticalLineText(canvas: Canvas, x:Float, text: String) {
        canvas.drawLine( x , marginPlot, x, height- marginPlot, paintAxis)
        canvas.drawText(text, x, height - marginPlot/2, paintTextDate)
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        var wColumn = 0f;
        var count = 0f
        var oldX = 0f
        var oldY = 0f
        pathFill.reset()
        if( countValues > 1  ){
            wColumn = ((width-2*marginPlot) / (countValues-1))
            pathFill.moveTo( marginPlot, height - marginPlot)
        }

        for (data in resultDraw3){
            var count = 0f
            var oldX = 0f
            var newX = 0f
            var oldY = 0f
            var newY = 0f
            paintLine.color =  Color.argb(255, rndColor.nextInt(256), rndColor.nextInt(256) , rndColor.nextInt(256))

            var date: LocalDate = minDate
            do {
                val strDate = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                val amount = data.value.firstOrNull { it.strDate == strDate }?.amount ?: 0
                newX = marginPlot+wColumn*count
                newY = marginPlot+ ((height-2*marginPlot)*(1-amount/maxValues)).toFloat()
                if(count != 0f) {
                    canvas.drawLine( oldX, oldY, newX, newY, paintLine )
                }
                oldX = newX
                oldY = newY
                //vertical line
                DrawVerticalLineText( canvas, oldX, strDate)
                date = date.plusDays(1)
                count++
            } while (date <= maxDate)

        }

//        for (data in resultDraw){
//            if(count != 0f) {
//                canvas.drawLine(
//                    oldX, oldY, oldX + wColumn * count,
//                    (height * (1 - data.value / maxValues)).toFloat(), paintLine
//                )
//            }
//            oldX = marginPlot+wColumn*count
//            oldY = (height*(1-data.value/maxValues)).toFloat()
//            //vertical line
//            DrawVerticalLineText( canvas, oldX, data.key.substring(8))
//
//            pathFill.lineTo( oldX, oldY)
//            count++
//        }
//
//        //fill plot
//        if(oldX != 0f){
//            pathFill.lineTo( oldX, oldY)
//            pathFill.lineTo( width - marginPlot, height - marginPlot)
//            canvas.drawPath(pathFill, paintFill)
//        }

        //horizontal lines and text
        DrawHorizontalLinesText(canvas)
    }

    companion object {
        const val TAG = "CustomView"
        const val marginView = 10f
        const val marginPlot = 100f
    }

}



