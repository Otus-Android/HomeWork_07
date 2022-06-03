package otus.homework.customview

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import kotlin.math.acos
import kotlin.math.pow
import kotlin.math.sqrt


class PieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr){

    private val blackPaint : Paint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 12f
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    private var list = mutableMapOf<String,Int>()
    private val widthDefault = (300 * context.resources.displayMetrics.density).toInt()
    private val heightDefault = (300 * context.resources.displayMetrics.density).toInt()
     private var myWidth: Int = 0
     private var myHeight: Int = 0
     private var summary: Int = 0
    var setOnSectorListener: ((String) -> Unit)? = null
    private var colors = listOf(Color.BLACK, Color.BLUE, Color.CYAN, Color.DKGRAY, Color.GRAY,
        Color.GREEN, Color.MAGENTA, Color.RED, Color.LTGRAY, Color.TRANSPARENT)

    private val rect = RectF()
    private var rotate = 0f

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val modeWidth = MeasureSpec.getMode(widthMeasureSpec)
        val modeHeight = MeasureSpec.getMode(heightMeasureSpec)
        val widthMeasure = MeasureSpec.getSize(widthMeasureSpec)
        val heightMeasure = MeasureSpec.getSize(heightMeasureSpec)
         myWidth = measureSize(modeWidth, widthMeasure, widthDefault)
         myHeight = measureSize(modeHeight, heightMeasure, heightDefault)

        setMeasuredDimension(myWidth, myHeight)
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

    override fun onDraw(canvas: Canvas?) {

        if (canvas == null) return
        var size = minOf(myHeight,myWidth)
        var startAngle = 0f
        rect.set(0f+10f,0f+10f,size.toFloat()-10f,size.toFloat()-10f)
        var count = 0
        list.forEach{
            val angle = ((it.value.toFloat()/summary.toFloat()) * 360f)
            blackPaint.color = colors[count]
            count++
            canvas.rotate(rotate,width/2f,height/2f)
            canvas.drawArc(rect,startAngle,angle,true,blackPaint)
            startAngle+=angle
        }

    }

    fun setValues(values: MutableMap<String,Int>,summary:Int){
        list.clear()
        list.putAll(values)
        this.summary = summary
        requestLayout()
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_UP) {
                if (pointInCircle(event, rect)){
                    var angle: Float
                    val x2 = event.x - rect.centerX()
                    val y2 = event.y - rect.centerY()
                    val d1 = sqrt((rect.centerY() * rect.centerY()).toDouble())
                    val d2 = sqrt((x2 * x2 + y2 * y2).toDouble())
                    angle = if (event.x >= rect.centerX()) {
                        Math.toDegrees(acos((-rect.centerY() * y2) / (d1 * d2))).toFloat()
                    } else
                        360 - Math.toDegrees(acos((-rect.centerY() * y2) / (d1 * d2))).toFloat()
                    angle-=90f
                    if (angle<0){ angle+=360f}
                    var startAngle = 0f
                    list.forEach{
                        val angle1 = ((it.value.toFloat()/summary.toFloat()) * 360f)
                        if (angle>startAngle&&angle<angle1+startAngle){
                            Log.d("Category", "Категория : ${it.key}")
                            setOnSectorListener?.invoke(it.key)
                        }
                        startAngle+=angle1
                    }
                    startAnimation()
                }
        }
        return true
    }
    private fun startAnimation(){
     ValueAnimator.ofFloat(0f,360f).apply {
         duration = 3000
         interpolator = LinearInterpolator()
         addUpdateListener {
             rotate = it.animatedValue as Float
             invalidate()
         }
         start()
     }
    }

    private fun pointInCircle(event: MotionEvent?, rect: RectF): Boolean {
        var distance = 0f
        var radius = 0f
        if (event != null) {
             distance = sqrt((rect.centerX() - event.x).toDouble().pow(2.0) + (rect.centerY() - event.y).toDouble()
                .pow(2.0)
            ).toFloat()
            radius = rect.width() / 2
        }
        return distance<=radius
    }

    override fun onSaveInstanceState(): Parcelable? {
        return   Bundle().apply {
            putParcelable("list", super.onSaveInstanceState())
            putString("myList", Gson().toJson(list))
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            val type: Type = object : TypeToken<MutableMap<String?, Int?>?>() {}.type
            list = Gson().fromJson(state.getString("myList"), type)
            Log.d("Category", "onRestoreInstanceState : $list")
            super.onRestoreInstanceState(state.getParcelable("list"))
        } else {
            super.onRestoreInstanceState(state)
        }
    }
}
