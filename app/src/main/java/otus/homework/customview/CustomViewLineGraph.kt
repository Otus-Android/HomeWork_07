package otus.homework.customview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Parcelable

import android.util.AttributeSet
import android.util.Log
import android.view.View
import java.text.SimpleDateFormat
import java.util.*


private const val TAG = "debug"

class CustomViewLineGraph(context: Context, attributeSet: AttributeSet) :
    View(context, attributeSet) {

    init {
        isSaveEnabled = true
    }

    var mPath = Path()
    private var userPaint = UserPaint()
    private val defaultWidth = resources.getDimension(R.dimen.pie_chart).toInt()
    private val defaultHeight = resources.getDimension(R.dimen.pie_chart).toInt()
    private var stateView = ListPaymentLineGraph(mutableListOf(), null)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        when {
            widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST -> {
                setMeasuredDimension(defaultWidth, defaultHeight)
            }

            widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.AT_MOST -> {
                setMeasuredDimension(widthSize, widthSize)
            }
            widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.EXACTLY -> {
                setMeasuredDimension(heightSize, heightSize)
            }

            widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY && widthSize
                    != heightSize -> {
                if (widthSize >= heightSize) setMeasuredDimension(heightSize, heightSize)
                else setMeasuredDimension(widthSize, widthSize)
            }
            (widthMode == MeasureSpec.EXACTLY || widthMode == MeasureSpec.AT_MOST) && heightMode
                    == MeasureSpec.UNSPECIFIED -> {
                setMeasuredDimension(widthSize, widthSize)
            }
            else ->
                if (widthSize >= heightMode) {
                    setMeasuredDimension(heightSize, heightSize)
                } else {
                    setMeasuredDimension(widthSize, widthSize)
                }
        }
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        val pHeight = height / 12F
        val pWidth = width / 12F
        val mOriginY = height - pHeight
        val mOriginX = pWidth
        val originXY = Pair(mOriginX, mOriginY)
        val listLine = mutableMapOf<Int, Int>()
        val dayInMonth = stateView.listLineGraph.firstOrNull()?.dayInMonth ?: 31
        val month = stateView.listLineGraph.firstOrNull()?.month ?: " "
        val listDay = stateView.listLineGraph.map { it.day }.distinct()

        listDay.forEachIndexed { _, day ->
            var acc = 0
            stateView.listLineGraph.forEach {
                if (it.day == day) {
                    acc += it.amount
                }
            }
            listLine[day] = acc
        }

        val maxY = listLine.values.maxOfOrNull { it * 2 } ?: 1
        val maxYText = (Math.round(maxY.toDouble() / 100) * 100).toString()

        //y-axis
        canvas.drawLine(
            mOriginX, pHeight, mOriginX, mOriginY, userPaint.redPaint
        )

        //x-axis
        canvas.drawLine(
            mOriginX,
            mOriginY,
            width - mOriginX,
            mOriginY, userPaint.redPaint
        )

        //graph
        mPath.reset()
        mPath.moveTo(originXY.first, originXY.second)
        listLine.forEach { (key, item) ->
            mPath.lineTo(
                originXY.first + (width - 2 * pWidth) / dayInMonth * key,
                originXY.second - ((originXY.second - pHeight) / (maxY) * item)
            )
        }
        canvas.drawPath(mPath, userPaint.blackPaintStroke.apply {
            isAntiAlias = true
            pathEffect = CornerPathEffect(50F)
        })

        //text maxY
        canvas.drawText(maxYText, originXY.first / 2, pHeight, userPaint.blackPaint.apply {
            textSize = 30f
        })

        //text month
        canvas.drawText(month, width / 2F, pHeight / 2, userPaint.blackPaint.apply {
            textSize = 30f
        })

        //text Y
        listLine.forEach {
            canvas.drawText(it.value.toString(),
                originXY.first / 2,
                originXY.second - ((originXY.second - pHeight) / (maxY) * it.value),
                userPaint.blackPaint.apply {
                    textSize = 30f
                })
        }

        //text X
        for (i in 0..dayInMonth) {
            canvas.drawText(i.toString(),
                originXY.first + ((width - originXY.first - pWidth) / dayInMonth * i),
                originXY.second + pHeight / 2,
                userPaint.blackPaint.apply {
                    textSize = 20f
                }
            )

        }
        invalidate()
    }

    fun setValue(value: List<PaymentLineGraph>) {
        stateView.listLineGraph.clear()
        stateView.listLineGraph.addAll(value)
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        return ListPaymentLineGraph(stateView.listLineGraph, superState)
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val listPaymentRestore = state as? ListPaymentLineGraph
        if (listPaymentRestore != null) {
            super.onRestoreInstanceState(listPaymentRestore.superSaveState)
            stateView.listLineGraph = listPaymentRestore.listLineGraph
        }
    }
}



