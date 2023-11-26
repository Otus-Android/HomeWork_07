package otus.homework.customview


import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class MyDiagrammView @JvmOverloads constructor (
    context: Context,
    attrs: AttributeSet? = null
): View(context, attrs) {

    private val values = ArrayList<Float>()

    private var paddingByHeight = 0f
    private var padding = 0f

    private lateinit var paintBackground : Paint
    private lateinit var paintMyRed: Paint
    private lateinit var paintMyOrange: Paint
    private lateinit var paintMySand: Paint
    private lateinit var paintMyPeach: Paint
    private lateinit var paintMyLemon: Paint
    private lateinit var paintMyLime: Paint
    private lateinit var paintMyWave: Paint
    private lateinit var paintMyOcean: Paint
    private lateinit var paintMyNight: Paint
    private lateinit var paintMyDeep: Paint

    private lateinit var paintStr: Paint

    private val listOfPaints = mutableListOf<Paint>()

    private var onePercent: Float = 0.0f

    private var scale: Float = 1f
    private var myX: Float = 0f
    private var myY: Float = 0f

    private var count = 0

    private val gestureDetector = GestureDetector(context, object :SimpleOnGestureListener(){
        override fun onDown(e: MotionEvent?): Boolean {
            scale+=2f
            e?.let {
                myX = e.x
                myY = e.y
            }
            Log.i(TAG, "doubleClicked")

            invalidate()
            return true


        }

        override fun onDoubleTap(e: MotionEvent?): Boolean {
            return super.onDoubleTap(e)
        }
    })


    init {
        if (isInEditMode) {
            setValues(listOf(4f, 2f, 1f, 5f, 0f, 2f))
        }
        setup(
            context
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val wMode = MeasureSpec.getMode(widthMeasureSpec)
        val hMode = MeasureSpec.getMode(heightMeasureSpec)
        val wSize = MeasureSpec.getSize(widthMeasureSpec)
        val hSize = MeasureSpec.getSize(heightMeasureSpec)

        println("$TAG wMode= ${MeasureSpec.toString(wMode)}  hMode=${MeasureSpec.toString(hMode)}  w=$wSize h=$hSize")
        when (hMode) {

            MeasureSpec.EXACTLY -> {
                paddingByHeight = if (hSize>wSize){
                    (hSize-wSize).toFloat()/2
                }else{
                    hSize.toFloat()/10
                }
                setMeasuredDimension(wSize, hSize)
            }
            MeasureSpec.AT_MOST -> {
                paddingByHeight = 0f
                val newH = wSize.coerceAtMost(hSize)
                val newW =  wSize.coerceAtMost(hSize)
                setMeasuredDimension(newW, newH)
            }
            MeasureSpec.UNSPECIFIED -> {
                val barWidth = 1f
                setMeasuredDimension((values.size * barWidth).toInt(), hSize)
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val worldHeight = height.toFloat() / 2
        val worldWidth = width.toFloat() / 2

        val circleLeft: Float
        val circleTop: Float
        val circleRight: Float
        val circleBottom: Float


        if (worldHeight < worldWidth) {
            padding = paddingByHeight + (worldWidth - worldHeight)
            circleTop = paddingByHeight
            circleBottom = height.toFloat() - paddingByHeight
            circleLeft = padding
            circleRight = width.toFloat() - padding

        } else {
            padding = worldWidth / 5
            circleLeft = padding
            circleTop = padding + paddingByHeight
            circleRight = width.toFloat() - padding
            circleBottom = height.toFloat() - paddingByHeight - padding
        }



        canvas.drawRGB(255, 255, 255)

        if (values.size == 0) return

        var startAngle = 0f
        var paint = listOfPaints[0]

        var paintIndx = 0


        var count = 0

        for (i in 0.. values.lastIndex) {

            val l =
                sqrt((myX - worldWidth) * (myX - worldWidth) + (myY - worldHeight) * (myY - worldHeight))
            val R = worldWidth - circleLeft

            var a = atan2(myY - worldHeight, myX - worldWidth)
            Log.i(TAG,"a= ${a * 180 / PI}")
            if (a<0){
                val d = 180 + a*180/PI
                a = (((180+d).toFloat()/180)*PI).toFloat()
            }
//Returns the angle theta of the polar coordinates (r, theta)
// that correspond to the rectangular coordinates (x, y)
// by computing the arc tangent of the value y / x;
// the returned value is an angle in the range from -PI to PI radians.
            //atan2(0.0, 0.0) is 0.0
            //atan2(0.0, x) is 0.0 for x > 0 and PI for x < 0
            //atan2(-0.0, x) is -0.0 for 'x > 0 and -PI for x < 0
            //atan2(y, +Inf) is 0.0 for 0 < y < +Inf and -0.0 for '-Inf < y < 0
            //atan2(y, -Inf) is PI for 0 < y < +Inf and -PI for -Inf < y < 0
            //atan2(y, 0.0) is PI/2 for y > 0 and -PI/2 for y < 0
            //atan2(+Inf, x) is PI/2 for finite xy
            //atan2(-Inf, x) is -PI/2 for finite x
            //atan2(NaN, x) and atan2(y, NaN) is NaN

            val endAngle = (values[i] / onePercent) * 3.6f

            Log.i(
                TAG,
                "item=${i})  R=$R,  startAngle=${startAngle*PI/180}, endAngle=${(startAngle + endAngle)*PI/180}    l=$l, a=$a  ang=${a}"
            )
            if ((l <= R) &&
                (a > startAngle*PI/180) && (a <= (startAngle + endAngle)*PI/180)
            ) {

                val koef = 50f
                canvas.drawArc(
                    circleLeft - koef,
                    circleTop - koef,
                    circleRight + koef,
                    circleBottom + koef,
                    startAngle,
                    endAngle,
                    true,
                    paint
                )

                Log.i(TAG, " YES")
            } else {

                canvas.drawArc(
                    circleLeft,
                    circleTop,
                    circleRight,
                    circleBottom,
                    startAngle,
                    endAngle,
                    true,
                    paint
                )


                Log.i(TAG, " NO")
            }
            Log.i(TAG, " ___________")



//            val ang = startAngle + ((item / onePercent) * 3.6f) / 2 + 180

//            val angRad = PI * ang / 180

//            val horiz = cos(angRad).toFloat() * 100f
//            val vert = sin(angRad).toFloat() * 100f

            startAngle += (values[i] / onePercent) * 3.6f
//
            if (paintIndx == listOfPaints.lastIndex) {
                paintIndx = 0
            } else {
                paintIndx++
            }
            paint = listOfPaints[paintIndx]
            count++

//        }
//        else{


            }




        Log.i(TAG,"count = $count  vals = $values")

        if (worldHeight < worldWidth) {
            canvas.drawOval(
                worldWidth - paddingByHeight * 2,
                worldHeight + paddingByHeight * 2,
                worldWidth + paddingByHeight * 2,
                worldHeight - paddingByHeight * 2,
                paintBackground
            )
        } else {

            val left = worldWidth - padding * 3
            val right = worldWidth + padding * 3
            val top = worldHeight + padding * 3
            val bottom = worldHeight - padding * 3



            //lines
            for (item in values){
                canvas.drawArc(
                    circleLeft ,
                    circleTop,
                    circleRight,
                    circleBottom,
                    startAngle,
                    (item/onePercent)*3.6f,
                    true,
                    paintStr
                )

                startAngle += (item / onePercent) * 3.6f
            }
//            white center
            canvas.drawOval(
                left,
                top,
                right,
                bottom,
                paintBackground
            )
        }

        canvas.drawOval(
            myX-15f,myY+15f,myX+15f,myY-15f,paintMyRed
        )

//        canvas.drawOval(worldWidth-20f,worldHeight-20f,worldWidth+20f,worldHeight+20f,paintMyNight)


    }

    fun setValues(values : List<Float>) {
        this.values.clear()
        this.values.addAll(values)
        val hundredPercent = this.values.sum()
        onePercent = hundredPercent/100
        requestLayout()
        invalidate()
    }

    private fun setup(
        context: Context
    ) {
        paintBackground = Paint().apply {
            color = context.getColor(R.color.white)
            style = Paint.Style.FILL
        }
        paintMyRed=Paint().apply {
            color = context.getColor(R.color.my_red)
            style = Paint.Style.FILL
        }
        paintMyOrange=Paint().apply {
            color = context.getColor(R.color.my_orange)
            style = Paint.Style.FILL
        }
        paintMySand=Paint().apply {
            color = context.getColor(R.color.my_sand)
            style = Paint.Style.FILL
        }
        paintMyPeach=Paint().apply {
            color = context.getColor(R.color.my_peach)
            style = Paint.Style.FILL
        }
        paintMyLemon=Paint().apply {
            color = context.getColor(R.color.my_lemon)
            style = Paint.Style.FILL
        }
        paintMyLime=Paint().apply {
            color = context.getColor(R.color.my_lime)
            style = Paint.Style.FILL
        }
        paintMyWave=Paint().apply {
            color = context.getColor(R.color.my_wave)
            style = Paint.Style.FILL
        }
        paintMyOcean=Paint().apply {
            color = context.getColor(R.color.my_ocean)
            style = Paint.Style.FILL
        }
        paintMyNight=Paint().apply {
            color = context.getColor(R.color.my_night)
            style = Paint.Style.FILL
        }
        paintMyDeep=Paint().apply {
            color = context.getColor(R.color.my_deep)
            style = Paint.Style.FILL
        }
        paintStr=Paint().apply {
            color = context.getColor(R.color.black)
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }

        listOfPaints.run {
            add(paintMyLime)
            add(paintMyRed)
            add(paintMySand)
            add(paintMyWave)
            add(paintMyOrange)
            add(paintMyOcean)
            add(paintMyPeach)
            add(paintMyLemon)
            add(paintMyNight)
            add(paintMyDeep)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        gestureDetector.onTouchEvent(event)
        return true
    }
}