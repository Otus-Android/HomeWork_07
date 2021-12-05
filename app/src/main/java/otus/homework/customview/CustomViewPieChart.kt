package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast

private const val TAG = "debug"
class CustomViewPieChart (context: Context, attributeSet: AttributeSet)
    : View (context,attributeSet){

    private val defaultWidth = 450
    private val defaultHeight = 450
    private var map = mutableMapOf<String, Float>()
    private var setCategory = mutableSetOf<String>()
    var widthPerView = 0
    var heightPerValue = 0

    val userPaint = UserPaint()
    val whitePaint = userPaint.whitePaint

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        when(widthMode){
            MeasureSpec.UNSPECIFIED -> {
                setMeasuredDimension(
                    when(map.size ){
                        0 -> 0
                        in 1..6 -> defaultWidth
                        else -> defaultWidth * 2
                    },
                    when(map.size ){
                        0 -> 0
                        in 1..6 -> defaultHeight
                        else -> defaultHeight * 2
                    })
            }

            MeasureSpec.AT_MOST ,
            MeasureSpec.EXACTLY -> {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            }
        }
    }


    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
    }

    override fun onDraw(canvas: Canvas) {
        val midHeight = height/2f
        val midWidth = width/2f

        var arc= 0f
        var n = 0
        while (arc<360){
            map[setCategory.elementAt(n)]?.let {
                var m = n
                canvas.drawArc(width.toFloat()*0.1f ,height.toFloat()*0.1f,width.toFloat()*0.9f ,
                    height.toFloat()*0.9f, arc ,  it, true, userPaint.color[n])
                arc +=it
            }
            n++
            canvas.save()
        }
        canvas.drawOval(width.toFloat()*0.15f ,height.toFloat()*0.15f,width.toFloat()*0.85f ,height.toFloat()*0.85f, whitePaint).run {
            setOnClickListener {
                Toast.makeText(context, "Oval", Toast.LENGTH_LONG).show()
            }
        }
        canvas.drawText("Total: ${map.values.sum()}", midWidth,midHeight, userPaint.blackPaint)
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return super.onTouchEvent(event)
    }

    fun setValue(value: MutableMap<String, Float>){
        map.clear()
        map.putAll(value)
        setCategory = map.keys
        Log.d(TAG, "$setCategory")

        requestLayout()
        invalidate()
    }
}