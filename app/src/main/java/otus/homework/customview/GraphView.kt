package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class GraphView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val widthDefault = (200 * context.resources.displayMetrics.density).toInt()
    private val heightDefault = (350 * context.resources.displayMetrics.density).toInt()
    private var spentList: ArrayList<PayLoad> = ArrayList()
    private var xMin = 0L
    private var xMax = 0L
    private var yMin = 0
    private var yMax = 0

    private val paintPoint = Paint().apply {
        color = Color.BLUE
    }
    private val paintLine = Paint().apply {
        color = Color.RED
        strokeWidth = 10f
        isAntiAlias = true
    }

    private var paint = Paint().apply {
        style = Paint.Style.STROKE
        flags = Paint.ANTI_ALIAS_FLAG
        color = Color.BLACK
        strokeWidth = 4 * context.resources.displayMetrics.density
    }
    private var path = Path()

    fun setValues(payLoad: List<PayLoad>){
        spentList.clear()
        spentList.addAll(payLoad)
        spentList.forEach{
            Log.d("GraphView", "Категория ${it.category}, цена ${it.amount} время ${it.time}")
        }
        yMax = payLoad.maxByOrNull { it.amount }?.amount?:0
        xMax = payLoad.maxByOrNull { it.time }?.time?:0
        xMin = payLoad.minByOrNull { it.time }?.time?:0
        yMin = payLoad.minByOrNull { it.amount }?.amount?:0
        Log.d("GraphView", "yMax $xMax, xMin $xMin")
        requestLayout()
        invalidate()
    }
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawLine(0f,0f,0f,height.toFloat(),paintLine)
        canvas?.drawLine(0f,height.toFloat(),width.toFloat(),height.toFloat(),paintLine)
        spentList.sortBy { it.time }
        spentList.forEachIndexed { index, it ->
                val realXmin =
                    (xMin.toDouble() / xMax.toDouble() * width.toDouble()).toFloat() - 0.01f
                val realYmin = (yMin.toDouble() / yMax.toDouble() * height.toDouble()).toFloat()
                val realYmax = (yMax.toDouble() / yMax.toDouble() * height.toDouble()).toFloat()
                val realXmax =
                    (xMax.toDouble() / xMax.toDouble() * width.toDouble()).toFloat() + 0.01f
                val realX = (it.time.toDouble() / xMax.toDouble() * width.toDouble()).toFloat()
                val realY = (it.amount.toDouble() / yMax.toDouble() * height.toDouble()).toFloat()
                var percentX = ((realX - realXmin) / (realXmax - realXmin)) * 100f
                var realXreal = percentX * realXmax / 100f


            if (index<spentList.size-1){
                var nextPoint =spentList[index+1]
                val realX2 = (nextPoint.time.toDouble() / xMax.toDouble() * width.toDouble()).toFloat()
                val realY2 = (nextPoint.amount.toDouble() / yMax.toDouble() * height.toDouble()).toFloat()
                var percentX2 = ((realX2 - realXmin) / (realXmax - realXmin)) * 100f
                var realX2real = percentX2 * realXmax / 100f
                canvas?.drawLine(realXreal,realY,realX2real,realY2,paintLine)
            }
            canvas?.drawCircle(realXreal, realY, 10f, paintPoint)

        }

    }
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val modeWidth = MeasureSpec.getMode(widthMeasureSpec)
        val modeHeight = MeasureSpec.getMode(heightMeasureSpec)
        val widthMeasure = MeasureSpec.getSize(widthMeasureSpec)
        val heightMeasure = MeasureSpec.getSize(heightMeasureSpec)
        val width = measureSize(modeWidth, widthMeasure, widthDefault)
        val height = measureSize(modeHeight, heightMeasure, heightDefault)
        setMeasuredDimension(width, height)
    }

    private fun measureSize(mode: Int, size: Int, sizeMax: Int) = when (mode) {
        MeasureSpec.EXACTLY -> {
            size
        }
        MeasureSpec.AT_MOST -> {
            if (sizeMax <= size) {
                sizeMax
            } else {
                size
            }
        }
        MeasureSpec.UNSPECIFIED -> {
            sizeMax
        }
        else -> {
            size
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        return   Bundle().apply {
            putParcelable("list", super.onSaveInstanceState())
            putString("myList", Gson().toJson(spentList))
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            val type: Type = object : TypeToken<List<PayLoad?>>() {}.type
            spentList = Gson().fromJson(state.getString("myList"), type)
            Log.d("Category", "onRestoreInstanceState : $spentList")
            super.onRestoreInstanceState(state.getParcelable("list"))
        } else {
            super.onRestoreInstanceState(state)
        }
    }
}
