package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

class MyDiagrammView @JvmOverloads constructor (
    context: Context,
    attrs: AttributeSet? = null
): View(context, attrs) {

    private val values = ArrayList<Expense>()

    private val paddingParameter = 10f
    private val widthOfCycleGraph = 100f

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

    private var clickedPointX: Float = 0f
    private var clickedPointY: Float = 0f



    private val gestureDetector = GestureDetector(context, object :SimpleOnGestureListener(){
        override fun onDown(e: MotionEvent?): Boolean {
            e?.let {
                clickedPointX = e.x
                clickedPointY = e.y

            }

            invalidate()
            return true
        }

        override fun onDoubleTap(e: MotionEvent?): Boolean {
            return super.onDoubleTap(e)
        }
    })


    init {
        if (isInEditMode) {
//            setValues(listOf(4f, 2f, 1f, 5f, 0f, 2f))
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
                setMeasuredDimension(wSize, hSize)
            }
            MeasureSpec.AT_MOST -> {
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

        canvas.drawRGB(255, 255, 255)

        if (values.isEmpty()) return

        val worldWidth = width.toFloat()
        val worldHeight = height.toFloat()
        val hCenter = worldHeight / 2
        val wCenter = worldWidth / 2

        val paddingWidth: Float
        val paddingHeight: Float

        if (hCenter < wCenter) {
            paddingHeight = worldHeight / paddingParameter
            paddingWidth = wCenter - hCenter + paddingHeight
        } else {
            paddingWidth = worldWidth / paddingParameter
            paddingHeight = hCenter - wCenter + paddingWidth
        }

        var startAngleG = 0f
        var paintIndex = 0
        var paint = listOfPaints[paintIndex]

        var chosenLeft : Float = 0f
        var chosenRight: Float = 0f
        var chosenTop: Float = 0f
        var chosenBottom: Float = 0f
        var chosenStartAngle = 0f
        var chosenEAngle = 0f
        var chosenPaint : Paint? = null

        for (i in 0..values.lastIndex) {
            val clickedXRelatevelyTheCenter = clickedPointX - wCenter
            val clickedYRelatevelyTheCenter = clickedPointY - hCenter
            val distanceToCenter =
                sqrt(clickedXRelatevelyTheCenter.pow(2) + clickedYRelatevelyTheCenter.pow(2))
            val graphRadius = wCenter - paddingWidth

            var angleToCenterRad = atan2(clickedYRelatevelyTheCenter, clickedXRelatevelyTheCenter)

            if (angleToCenterRad < 0) {
                val angleToCenterG = (180 + angleToCenterRad * 180 / PI).toFloat()
                angleToCenterRad = (((180 + angleToCenterG) / 180) * PI).toFloat()
            }

            val endAngleG = (values[i].amount / onePercent) * 3.6f

            Log.i(
                TAG,
                "item=${i})  R=$graphRadius,  startAngle=${startAngleG}, endAngle=${ endAngleG}    l=$distanceToCenter, a=$angleToCenterRad  ang=${angleToCenterRad*180/PI}"
            )

            val innerCycleRadius = (worldWidth - 2*paddingWidth - 2*widthOfCycleGraph) / 2

            val startAngleRad = startAngleG * PI / 180
            val endAngleRad = endAngleG * PI / 180

            if (checkIfTouchedInPieceOfGraph(
                    distanceToCenter,
                    graphRadius,
                    innerCycleRadius,
                    angleToCenterRad,
                    startAngleRad,
                    endAngleRad
                )
            ) {
                chosenLeft = paddingWidth - 20f
                chosenTop = paddingHeight - 20f
                chosenRight = worldWidth - paddingWidth + 20f
                chosenBottom = worldHeight - paddingHeight + 20f
                chosenPaint = paint
                chosenStartAngle = startAngleG - 10f
                chosenEAngle = endAngleG + 20f

            } else {
                drawPieceOfGraph(
                    canvas,
                    paddingWidth,
                    paddingHeight,
                    worldWidth,
                    worldHeight,
                    startAngleG,
                    endAngleG,
                    paint
                )
            }

            startAngleG += endAngleG
            if (paintIndex == listOfPaints.lastIndex) {
                paintIndex = 0
            } else {
                paintIndex++
            }
            paint = listOfPaints[paintIndex]


            drawInnerBackgroundCircles(
                canvas,
                paddingWidth,
                paddingHeight,
                worldWidth,
                worldHeight
            )

            drawChosenPiece(
                chosenPaint,
                canvas,
                chosenLeft,
                chosenTop,
                chosenRight,
                chosenBottom,
                chosenStartAngle,
                chosenEAngle
            )
        }
    }

    private fun checkIfTouchedInPieceOfGraph(
        distanceToCenter: Float,
        graphRadius: Float,
        innerCycleRadius: Float,
        angleToCenterRad: Float,
        startAngleRad: Double,
        endAngleRad: Double
    ): Boolean {
        return (((distanceToCenter <= graphRadius) && (distanceToCenter >= innerCycleRadius)) &&
                (angleToCenterRad > startAngleRad) && (angleToCenterRad <= (startAngleRad + endAngleRad))
                )
    }

    private fun drawPieceOfGraph(
        canvas: Canvas,
        paddingWidth: Float,
        paddingHeight: Float,
        worldWidth: Float,
        worldHeight: Float,
        startAngleG: Float,
        endAngleG: Float,
        paint: Paint
    ) {
        canvas.drawArc(
            paddingWidth,
            paddingHeight,
            worldWidth - paddingWidth,
            worldHeight - paddingHeight,
            startAngleG,
            endAngleG,
            true,
            paint
        )
        canvas.drawArc(
            paddingWidth,
            paddingHeight,
            worldWidth - paddingWidth,
            worldHeight - paddingHeight,
            startAngleG,
            endAngleG,
            true,
            paintStr
        )
    }

    private fun drawInnerBackgroundCircles(
        canvas: Canvas,
        paddingWidth: Float,
        paddingHeight: Float,
        worldWidth: Float,
        worldHeight: Float
    ) {
        canvas.drawOval(
            paddingWidth + widthOfCycleGraph,
            paddingHeight + widthOfCycleGraph,
            worldWidth - paddingWidth - widthOfCycleGraph,
            worldHeight - paddingHeight - widthOfCycleGraph,
            paintBackground
        )
        canvas.drawOval(
            paddingWidth + widthOfCycleGraph,
            paddingHeight + widthOfCycleGraph,
            worldWidth - paddingWidth - widthOfCycleGraph,
            worldHeight - paddingHeight - widthOfCycleGraph,
            paintStr
        )
    }

    private fun drawChosenPiece(
        chosenPaint: Paint?,
        canvas: Canvas,
        chosenLeft: Float,
        chosenTop: Float,
        chosenRight: Float,
        chosenBottom: Float,
        chosenStartAngle: Float,
        chosenEAngle: Float
    ) {
        chosenPaint?.let {
            canvas.drawArc(
                chosenLeft,
                chosenTop,
                chosenRight,
                chosenBottom,
                chosenStartAngle,
                chosenEAngle,
                true,
                chosenPaint
            )
            canvas.drawArc(
                chosenLeft,
                chosenTop,
                chosenRight,
                chosenBottom,
                chosenStartAngle,
                chosenEAngle,
                true,
                paintStr
            )
            val koef = 1.4f * widthOfCycleGraph
            canvas.drawArc(
                chosenLeft + koef,
                chosenTop + koef,
                chosenRight - koef,
                chosenBottom - koef,
                chosenStartAngle,
                chosenEAngle,
                true,
                paintStr
            )

            canvas.drawOval(
                chosenLeft + koef,
                chosenTop + koef,
                chosenRight - koef,
                chosenBottom - koef,
                paintBackground
            )
        }
    }


    fun setValues(values : List<Expense>) {
        this.values.clear()
        this.values.addAll(values)

        var hundredPercent = 0f
        values.forEach { hundredPercent+=it.amount }
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