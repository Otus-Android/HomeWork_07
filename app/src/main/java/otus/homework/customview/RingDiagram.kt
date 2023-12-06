package otus.homework.customview

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Parcelable
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

class RingDiagram @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val pad = 20f
    private lateinit var itemList: ItemList
    private var ringWidth: Int = 0.dp
    private val chosenPieceShelf: Float
        get() = ringWidth / 10f

    private lateinit var paintBackground: Paint
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

    private lateinit var paintTextMain: Paint
    private lateinit var paintTextNameOfCategory: Paint
    private lateinit var paintTextAmount: Paint

    private lateinit var paintStr: Paint

    private val listOfPaints = mutableListOf<Paint>()

    private val onePercent: Float
        get() = itemList.onePercent

    private var clickedPointX: Float = 0f
    private var clickedPointY: Float = 0f
    private var myPaddingWith: Float = 0f
    private var chosenPiece: Item? = null

    var chooseCategoryCallback: ((Item) -> Unit)? = null
    var switchCatsCallback: (() -> Unit)? = null

    val typedArray: TypedArray

    init {

        if (isInEditMode) {
//            setValues(listOf(Expense(12,"Cinema",250,"fun",0L))) {
//            }
        }
        setup(
            context
        )
        typedArray = context.obtainStyledAttributes(attrs, R.styleable.DiagramViewGroup_Layout)
        typedArray.recycle()
    }


    private val gestureDetector = GestureDetector(context, object : SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent?): Boolean {

            e?.let {
                clickedPointX = e.x
                clickedPointY = e.y
                val hCenter = height / 2
                val wCenter = width / 2

                val clickedXRelatevelyTheCenter = clickedPointX - wCenter
                val clickedYRelatevelyTheCenter = clickedPointY - hCenter
                val distanceToCenter =
                    sqrt(clickedXRelatevelyTheCenter.pow(2) + clickedYRelatevelyTheCenter.pow(2))
                val graphRadius = wCenter - myPaddingWith
//
                if (distanceToCenter < graphRadius - ringWidth) {
                    switchCatsCallback!!.invoke()
                    chosenPiece = null
                    invalidate()
                    return true
                }

                var angleToCenterRad =
                    atan2(clickedYRelatevelyTheCenter, clickedXRelatevelyTheCenter)

                if (angleToCenterRad < 0) {
                    val angleToCenterG = (180 + angleToCenterRad * 180 / PI).toFloat()
                    angleToCenterRad = (((180 + angleToCenterG) / 180) * PI).toFloat()
                }

                var angleStart = 0f
                val values = itemList.pieces
                for (i in 0..values.lastIndex) {
                    val angleRad: Float =
                        (values[i].amount / onePercent) * 3.6f * PI.toFloat() / 180
                    val angleEnd = angleStart + angleRad

                    if ((angleToCenterRad >= angleStart) && (angleToCenterRad < angleEnd)) {
                        val innerCycleRadius = (width - 2 * myPaddingWith - 2 * ringWidth) / 2



                        if (checkIfTouchedInPieceOfGraph(
                                distanceToCenter,
                                graphRadius,
                                innerCycleRadius,
                            )
                        ) {
                            Log.d(TAG, "was chosen : ${values[i].name}")
//                            callback?.invoke(values[i])
                            chosenPiece = values[i]
                            chooseCategoryCallback?.invoke(values[i])
                            invalidate()
                            return true
                        }
                    }
                    angleStart = angleEnd
                }
                chosenPiece = null
            }
            invalidate()
            return true
        }

        override fun onDoubleTap(e: MotionEvent?): Boolean {
            return super.onDoubleTap(e)
        }
    })


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val wMode = MeasureSpec.getMode(widthMeasureSpec)
        val hMode = MeasureSpec.getMode(heightMeasureSpec)
        val wSize = MeasureSpec.getSize(widthMeasureSpec)
        val hSize = MeasureSpec.getSize(heightMeasureSpec)

        log(
            "onMeasure______ wMode= ${MeasureSpec.toString(wMode)}  hMode=${
                MeasureSpec.toString(
                    hMode
                )
            }  wSize=$wSize hSize=$hSize"
        )

        when (hMode) {
            MeasureSpec.EXACTLY -> {
                setMeasuredDimension(wSize, hSize)
            }

            MeasureSpec.AT_MOST -> {
                val size = wSize.coerceAtMost(hSize)
                ringWidth = size / 6
                log("ring width view = $ringWidth")
                setMeasuredDimension(size, size)
            }

            MeasureSpec.UNSPECIFIED -> {
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRGB(255, 255, 255)
        val values = itemList.pieces
        if (values.isEmpty()) return


        myPaddingWith = 0f

        var startAngleG = 0f
        var paintIndex = 0
        var paint = listOfPaints[paintIndex]

        var chosenPieceAngleStart = 0f
        var chosenPieceAngleEnd = 0f
        var chosenPiecePaint: Paint? = null

        values.forEach { currentItem ->
            val endAngleG = (currentItem.amount / onePercent) * 3.6f

            // save params of chosen piece for furthest drawing
            if ((chosenPiece != null) && (chosenPiece == currentItem)) {
                chooseCategoryCallback?.invoke(currentItem)
                chosenPiecePaint = paint
                chosenPieceAngleStart = startAngleG - 10f
                chosenPieceAngleEnd = endAngleG + 20f
            }

            // draw current piece
            drawPiece(
                canvas,
                startAngleG,
                endAngleG,
                paint
            )
            //change angles
            startAngleG += endAngleG
            if (paintIndex == listOfPaints.lastIndex) {
                paintIndex = 0
            } else {
                paintIndex++
            }
            paint = listOfPaints[paintIndex]
        }

        drawCenter(canvas)
        //if there is chosen piece draw it
        if (chosenPiece != null) {
            drawChosenPiece(
                canvas,
                chosenPieceAngleStart,
                chosenPieceAngleEnd,
                chosenPiecePaint!!,
            )


        }
        drawTextSum(canvas)
        drawCategoryNameText(canvas)
    }

    private fun drawTextSum(
        canvas: Canvas,
    ) {
        val diameter = width - 2 * (pad + ringWidth)
        val r = diameter / 2
        val baseLine = height / 2 - r / 2
        val spaceForHeader = sqrt(3.0f) * r
        val text = if (chosenPiece != null) {
            "${chosenPiece!!.amount} ₽"
        } else {
            "${itemList.total} ₽"
        }
        var textWidth = paintTextMain.measureText(text)
        while (textWidth > 0.6f * spaceForHeader) {
            paintTextMain.textSize = paintTextMain.textSize - 1f
            textWidth = paintTextMain.measureText(text)
        }
        val x0 = width / 2 - textWidth / 2
        canvas.drawText(text, x0, baseLine, paintTextMain)
    }

    private fun drawCategoryNameText(
        canvas: Canvas,
    ) {
        val text = if (chosenPiece != null) {
            chosenPiece!!.name
        } else {
            "Всего"
        }
        val diameter = width - 2 * (pad + ringWidth)
        val r = diameter / 2
        val baseLine = height / 2f - r / 6
        paintTextNameOfCategory.textSize = 70f
        var textNameWidth = paintTextNameOfCategory.measureText(text)
        while (textNameWidth > 0.8f * diameter) {
            paintTextNameOfCategory.textSize = paintTextNameOfCategory.textSize - 1f
            textNameWidth = paintTextNameOfCategory.measureText(text)
        }
        val x0 = width / 2f - textNameWidth / 2f
        canvas.drawText(text, x0, baseLine, paintTextNameOfCategory)
    }


    private fun checkIfTouchedInPieceOfGraph(
        distanceToCenter: Float,
        graphRadius: Float,
        innerCycleRadius: Float,
    ): Boolean {
        return (distanceToCenter <= graphRadius) && (distanceToCenter >= innerCycleRadius)
    }

    private fun drawPiece(
        canvas: Canvas,
        angleStart: Float,
        angleEnd: Float,
        paint: Paint
    ) {
        val l = pad
        val t = pad
        val r = width - pad
        val b = height - pad
        canvas.drawArc(
            l, t, r, b,
            angleStart,
            angleEnd,
            true,
            paint
        )
        canvas.drawArc(
            l, t, r, b,
            angleStart,
            angleEnd,
            true,
            paintStr
        )
    }


    private fun drawCenter(
        canvas: Canvas,
    ) {
        val l = pad + ringWidth
        val t = pad + ringWidth
        val r = width - pad - ringWidth
        val b = height - pad - ringWidth

        canvas.drawOval(
            l, t, r, b,
            paintBackground
        )
        canvas.drawOval(
            l, t, r, b,
            paintStr
        )
    }

    private fun drawChosenPiece(
        canvas: Canvas,
        chosenPieceAngleStart: Float,
        chosenPieceAngleEnd: Float,
        chosenPiecePaint: Paint,
    ) {
        val l = pad - chosenPieceShelf
        val t = pad - chosenPieceShelf
        val r = width - pad + chosenPieceShelf
        val b = height - pad + chosenPieceShelf

        canvas.drawArc(
            l, t, r, b,
            chosenPieceAngleStart,
            chosenPieceAngleEnd,
            true,
            chosenPiecePaint
        )
        canvas.drawArc(
            l, t, r, b,
            chosenPieceAngleStart,
            chosenPieceAngleEnd,
            true,
            paintStr
        )
        //draw center if smth chosen
        val differenceBetweenBigAndSmallArc = chosenPieceShelf + ringWidth + chosenPieceShelf
        canvas.drawArc(
            l + differenceBetweenBigAndSmallArc,
            t + differenceBetweenBigAndSmallArc,
            r - differenceBetweenBigAndSmallArc,
            b - differenceBetweenBigAndSmallArc,
            chosenPieceAngleStart,
            chosenPieceAngleEnd,
            true,
            paintStr
        )

        canvas.drawOval(
            l + differenceBetweenBigAndSmallArc,
            t + differenceBetweenBigAndSmallArc,
            r - differenceBetweenBigAndSmallArc,
            b - differenceBetweenBigAndSmallArc,
            paintBackground
        )
    }

    fun setValues(_itemList: ItemList,) {
        itemList = _itemList
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

        paintTextMain = Paint().apply {
            color = context.getColor(R.color.black)
            style = Paint.Style.STROKE
            textSize = 188f
        }

        paintTextAmount = Paint().apply {
            color = context.getColor(R.color.black)
            style = Paint.Style.FILL
            textSize = 130f
        }
        paintTextNameOfCategory = Paint().apply {
            color = context.getColor(R.color.black)
            style = Paint.Style.FILL
            textSize = 130f
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

    override fun onRestoreInstanceState(state: Parcelable?) {
        val analyticalPieChartState = state as? AnalyticalPieChartState
        super.onRestoreInstanceState(analyticalPieChartState?.superState ?: state)

        chosenPiece = analyticalPieChartState?.expense
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        return AnalyticalPieChartState(superState, chosenPiece)
    }
    private fun log(text: String){
        Log.d(TAG, text)
    }

    fun Any.TAG() = this.javaClass.name
}



class AnalyticalPieChartState(
    private val superSavedState: Parcelable?,
    val expense: Item?
) : View.BaseSavedState(superSavedState), Parcelable {
}



