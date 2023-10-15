package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import otus.homework.customview.utils.dp


class LineChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private var itemsData = mutableMapOf<String, Int>()

    fun setData(data: Map<String, Int>) {
        this.itemsData.putAll(data)
        requestLayout()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        var width = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        var height = MeasureSpec.getSize(heightMeasureSpec)

        when (widthMode) {
            MeasureSpec.AT_MOST, MeasureSpec.UNSPECIFIED -> if (width > height) {
                width = ((itemsData.size) * 50.dp).toInt()
            }
            MeasureSpec.EXACTLY -> { /* leave exactly width */
            }
        }
        when (heightMode) {
            MeasureSpec.AT_MOST, MeasureSpec.UNSPECIFIED -> if (height > width) {
                height = ((itemsData.size) * 50.dp).toInt()
            }
            MeasureSpec.EXACTLY -> { /* leave exactly height */
            }
        }

        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        val padding = 10.dp

        // draw axis X,Y
        val axisPaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 3.dp
        }

        canvas.drawLine(padding, height-padding, width-padding, height-padding, axisPaint)
        canvas.drawLine(padding, padding, padding, height-padding, axisPaint)

        // draw netting
        val nettingPaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 1.dp
            pathEffect = DashPathEffect(floatArrayOf(30f, 10f), 0f)
        }
        val periodX = 1..31
        val spacingX = (width-2*padding) / periodX.last
        for (i in periodX) {
            val x = padding+i*spacingX
            canvas.drawLine(x, padding, x, height-padding, nettingPaint)
        }

        val maxY = itemsData.values.max()/1000+2
        val periodY = 0..maxY
        val lineYLength = height-2*padding
        val spacingY = lineYLength / maxY
        for (i in periodY) {
            val y = padding + i*spacingY
            canvas.drawLine(padding, y, width-padding, y, nettingPaint)
        }

        // draw point and lines
        val circlePaint = Paint().apply {
            style = Paint.Style.FILL
            color = context.getColor(R.color.blue)
        }

        var firstX = 0f
        var firstY = 0f

        val linePaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 2.dp
            color = context.getColor(R.color.blue)
        }
        val textPaint: Paint = Paint().apply {
            textSize = 13.dp
            color = Color.WHITE
            textAlign = Paint.Align.LEFT
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val rectPaint = Paint().apply {
            style = Paint.Style.FILL
            color = Color.BLUE
            alpha = 0x90
        }
        val fm = Paint.FontMetrics()
        textPaint.getFontMetrics(fm)

        itemsData.forEach { (data, expenses) ->
            val day = data.split(".")[0].toInt()
            val x = padding + day*spacingX
            val y = (padding + lineYLength - expenses/1000.0*spacingY).toFloat()
            if(firstX != 0f && firstY != 0f){
                canvas.drawLine(firstX, firstY, x, y, linePaint)
            }
            firstX = x
            firstY = y
            canvas.drawCircle(x, y, 10f, circlePaint)
            val widthText = textPaint.measureText(expenses.toString())


            val margin = 5.dp
            canvas.drawRect(
                x + margin,
                y + fm.top - margin,
                x + widthText + 3 * margin,
                y + fm.bottom + margin,
                rectPaint
            )
            canvas.drawText(
                expenses.toString(),
                x + padding,
                y,
                textPaint
            )
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        return LineChartSavedState(super.onSaveInstanceState())
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state !is LineChartView.LineChartSavedState) {
            return super.onRestoreInstanceState(state)
        }
        super.onRestoreInstanceState(state.superState)
        setData(state.savedData)
    }


    private inner class LineChartSavedState : BaseSavedState {
        val savedData = mutableMapOf<String, Int>()

        constructor(source: Parcelable?) : super(source) {
            savedData.putAll(itemsData)
        }

        private constructor(parcelIn: Parcel) : super(parcelIn) {
            parcelIn.readMap(savedData, ClassLoader.getSystemClassLoader())
        }

        override fun writeToParcel(parcelOut: Parcel, flags: Int) {
            super.writeToParcel(parcelOut, flags)
            parcelOut.writeMap(savedData)
        }

        @JvmField
        val CREATOR: Parcelable.Creator<LineChartSavedState?> =
            object : Parcelable.Creator<LineChartSavedState?> {
                override fun createFromParcel(parcelIn: Parcel): LineChartSavedState {
                    return LineChartSavedState(parcelIn)
                }

                override fun newArray(size: Int): Array<LineChartSavedState?> {
                    return arrayOfNulls(size)
                }
            }
    }
}
