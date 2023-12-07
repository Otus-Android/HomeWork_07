package otus.homework.customview

import android.content.Context
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

    private val padding: Float
        get() = chosenPieceShelf
    private lateinit var itemList: ItemList
    private var ringWidth: Int = 0.dp
    private val chosenPieceShelf: Float
        get() = ringWidth / 10f

    private lateinit var paintForPieces: Paint
    private  lateinit var colors: List<Int>

    private lateinit var paintBackground: Paint


    private lateinit var paintTextMain: Paint
    private lateinit var paintTextNameOfCategory: Paint
    private lateinit var paintTextAmount: Paint

    private lateinit var paintStr: Paint

    private val onePercent: Float
        get() = itemList.onePercent

    private var clickedPointX: Float = 0f
    private var clickedPointY: Float = 0f

    private var chosenPiece: Item? = null

    var chooseCategoryCallback: ((Item) -> Unit)? = null
    var switchCatsCallback: (() -> Unit)? = null

    init {
        if (isInEditMode) {
            setValues(ItemList(listOf(Item("eda", 1000), Item("car", 15000)), 12))
        }
        setup(
            context
        )
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
                val graphRadius = wCenter - padding
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
                        val innerCycleRadius = (width - 2 * padding - 2 * ringWidth) / 2



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

        var startAngleG = 0f
        var colorIndex = 0
        var chosenPieceAngleStart = 0f
        var chosenPieceAngleEnd = 0f
        var chosenColorIndex = 0

        values.forEach { currentItem ->
            val endAngleG = (currentItem.amount / onePercent) * 3.6f

            // save params of chosen piece for furthest drawing
            if ((chosenPiece != null) && (chosenPiece == currentItem)) {
                chooseCategoryCallback?.invoke(currentItem)
                chosenColorIndex = colorIndex
                chosenPieceAngleStart = startAngleG - 10f
                chosenPieceAngleEnd = endAngleG + 20f
            }

            // draw current piece
            drawPiece(
                canvas,
                startAngleG,
                endAngleG,
                colorIndex
            )
            //change angles
            startAngleG += endAngleG
            if (colorIndex == colors.lastIndex) {
                colorIndex = 0
            } else {
                colorIndex++
            }
        }

        drawCenter(canvas)
        //if there is chosen piece draw it
        if (chosenPiece != null) {
            drawChosenPiece(
                canvas,
                chosenPieceAngleStart,
                chosenPieceAngleEnd,
                chosenColorIndex,
            )


        }
        drawTextSum(canvas)
        drawCategoryNameText(canvas)
    }

    private fun drawTextSum(
        canvas: Canvas,
    ) {
        val diameter = width - 2 * (padding + ringWidth)
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
        val diameter = width - 2 * (padding + ringWidth)
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
        colorIndex: Int
    ) {
        val l = padding
        val t = padding
        val r = width - padding
        val b = height - padding
        paintForPieces.color = colors[colorIndex]
        canvas.drawArc(
            l, t, r, b,
            angleStart,
            angleEnd,
            true,
            paintForPieces
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
        val l = padding + ringWidth
        val t = padding + ringWidth
        val r = width - padding - ringWidth
        val b = height - padding - ringWidth

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
        colorIndex: Int,
    ) {
        val l = padding - chosenPieceShelf
        val t = padding - chosenPieceShelf
        val r = width - padding + chosenPieceShelf
        val b = height - padding + chosenPieceShelf

        paintForPieces.color = colors[colorIndex]
        canvas.drawArc(
            l, t, r, b,
            chosenPieceAngleStart,
            chosenPieceAngleEnd,
            true,
            paintForPieces
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

    fun setValues(_itemList: ItemList) {
        itemList = _itemList
        requestLayout()
        invalidate()
    }

    private fun setup(
        context: Context
    ) {
        paintForPieces = Paint().apply {
            color = context.getColor(R.color.white)
            style = Paint.Style.FILL
        }

        colors = listOf(
            context.getColor(R.color.my_red),
            context.getColor(R.color.my_orange),
            context.getColor(R.color.my_sand),
            context.getColor(R.color.my_peach),
            context.getColor(R.color.my_lemon),
            context.getColor(R.color.my_lime),
            context.getColor(R.color.my_wave),
            context.getColor(R.color.my_ocean),
            context.getColor(R.color.my_night),
            context.getColor(R.color.my_deep),
            context.getColor(R.color.black),
        )


        paintBackground = Paint().apply {
            color = context.getColor(R.color.white)
            style = Paint.Style.FILL
        }

        paintStr = Paint().apply {
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

    private fun log(text: String) {
        Log.d(TAG, text)
    }
}


class AnalyticalPieChartState(
    superSavedState: Parcelable?,
    val expense: Item?
) : View.BaseSavedState(superSavedState), Parcelable {
}



