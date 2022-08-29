package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.google.gson.Gson
import java.lang.Integer.min
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.sqrt
import kotlin.random.Random


class CustomView1 @JvmOverloads constructor (context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0): View(context, attrs, defStyleAttr) {

    private val dataPayLoad: Array<PayLoad>
    init {
        val gson = Gson()
        val buffer: String = resources.openRawResource(R.raw.payload).bufferedReader().use { it.readText() }
        dataPayLoad = gson.fromJson(buffer, Array<PayLoad>::class.java)
    }

    private val sumAmounts = dataPayLoad.sumOf { it.amount }
    private var dataPayLoadDraw = ArrayList<PayLoadDraw>()
    private var radius: Float = 1.0f
    private val rndColor = Random
    private val paintArc: Paint = Paint().apply{
        strokeWidth = 0f
        style = Paint.Style.FILL
    }

    init {
        var sumAngle: Float = 0f
        for (data in dataPayLoad) {
            val angle = data.amount.toFloat() / (sumAmounts) * 360f
            val dataDraw = PayLoadDraw(data.category, startAngle = sumAngle, fillAngle = angle)
            dataPayLoadDraw.add(dataDraw)
            sumAngle += angle
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        super.onSaveInstanceState()
        val bundle = Bundle()
        bundle.putParcelable("superState", super.onSaveInstanceState())
        //bundle.putParcelableArrayList("dataPayLoadDraw", this.dataPayLoadDraw) // ... save stuff
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        var state = state
        if (state is Bundle) // implicit null check
        {
            val bundle = state
            //this.stuff = bundle.getInt("stuff") // ... load stuff
            state = bundle.getParcelable("superState")
        }
        super.onRestoreInstanceState(state)
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
                //super.onMeasure(widthMeasureSpec, heightMeasureSpec)
                setMeasuredDimension(widthSize, heightSize)
            }
        }
        radius = min(widthSize,heightSize).toFloat()/2 - 30
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (data in dataPayLoadDraw){
            canvas.drawArc(width/2-radius, height/2-radius, width/2+radius,
                height/2+radius, data.startAngle, data.fillAngle, true,
                paintArc.apply {
                    color = Color.argb(255, rndColor.nextInt(256), rndColor.nextInt(256) , rndColor.nextInt(256))
                }
            )
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        Log.d("child", "on touch event")
        if (event.action === MotionEvent.ACTION_DOWN) {

            val x = (event.x-width/2).toDouble()
            val y = (event.y-height/2).toDouble()
            if (sqrt(x*x+y*y) <= radius){
                var angle = atan2(y, x ) * 180 / PI
                if(angle<0) angle += 360
                var text: String = ""
                for (data in dataPayLoadDraw) {
                    if(text.isEmpty()) text = data.category
                    if(data.startAngle > angle) {
                        Toast.makeText(this.context, "Category: $text", Toast.LENGTH_SHORT).show();
                        text = ""
                        break
                    }
                    text = data.category
                }
                if(text.isNotEmpty()) {
                    Toast.makeText(this.context, "Category: $text", Toast.LENGTH_SHORT).show();
                }
                return true
            }
        }
        return false
    }

    companion object {
        const val TAG = "CustomView"
    }

}

private fun Parcelable.putParcelableArrayList(s: String, dataPayLoadDraw: ArrayList<PayLoadDraw>) {

}

data class PayLoadDraw(
    val category: String,
    val startAngle: Float,
    val fillAngle: Float
)

