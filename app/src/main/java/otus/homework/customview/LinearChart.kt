package otus.homework.customview

import android.content.Context
import android.content.res.Configuration
import android.graphics.*
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.os.bundleOf
import kotlinx.parcelize.Parcelize
import otus.homework.customview.data.Segment

class LinearChart @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private var orientationMode: Int = -1
    private var segmentList = ArrayList<Segment>()

    private val axePaint = Paint().apply {
        color = Color.GRAY
        alpha = 100
        style = Paint.Style.FILL_AND_STROKE
        strokeWidth = 5F
    }

    private val scaleTextPaint = Paint().apply {
        color = Color.GRAY
        textSize = 40f
        style = Paint.Style.FILL_AND_STROKE
        strokeWidth = 1F
    }

    private val linesPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        strokeWidth = 5F
    }

    var maxValue = 0F
    var daysCount = 0

    fun setData(segmentList: ArrayList<Segment>, orientation: Int) {
        this.segmentList = segmentList
        orientationMode = orientation

        maxValue = segmentList.maxOf { it.value }

        val overallSeconds = segmentList.maxOf { it.time } - segmentList.minOf { it.time }
        daysCount = overallSeconds / 3600

        invalidate()
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        var heightSize = MeasureSpec.getSize(heightMeasureSpec)

        if (orientationMode == Configuration.ORIENTATION_PORTRAIT) {
            heightSize /= 2
        }

        when(widthMode) {
            MeasureSpec.AT_MOST -> {
                setMeasuredDimension(widthSize, heightSize)
            }
            MeasureSpec.EXACTLY -> {
                setMeasuredDimension(widthSize, heightSize)
            }
            MeasureSpec.UNSPECIFIED -> {}
        }

        when(heightMode) {
            MeasureSpec.AT_MOST -> {
                setMeasuredDimension(widthSize, heightSize)
            }
            MeasureSpec.EXACTLY -> {
                setMeasuredDimension(widthSize, heightSize)
            }
            MeasureSpec.UNSPECIFIED -> {}
        }
    }

    val padding = 150F

    override fun onDraw(canvas: Canvas?) {
        if (canvas == null) return

        drawAxes(canvas)
        drawAxeValues(canvas)

        val axeYHeight = (height - padding) - padding
        segmentList.forEach { segment ->
            linesPaint.color = segment.color
            val destY = axeYHeight - (segment.value / maxValue) * axeYHeight
            canvas.drawLine(padding, height - padding, width - padding, destY + padding, linesPaint)
        }
    }

    private fun drawAxes(canvas: Canvas) {
        //Axe X
        canvas.drawLine(
            padding,
            height.toFloat() - padding,
            width.toFloat() - padding,
            height.toFloat() - padding,
            axePaint)
        //Axe Y
        canvas.drawLine(
            padding,
            height.toFloat() - padding,
            padding,
            padding,
            axePaint)
    }


    /*
    Эмпирическим путем было выявлено, что оптимальное количество меток на шкале является 4, для отображения на малом экране,
    drawTextSetOff - параметр смещения текста относительно наших координат,
    так как drawText у канвас начинает отрисовку по заданным координат, то у нас получается, что текст выходит за границы нашей оси
     */
    private fun drawAxeValues(canvas: Canvas) {
        var markDivision = 4
        var drawTextSetOff = 40f
        drawValuesX(canvas, markDivision, drawTextSetOff)
        drawValuesY(canvas, markDivision, drawTextSetOff)
    }

    private fun drawValuesY(canvas: Canvas, markDivision: Int, drawTextSetOff: Float) {
        val barDiv = (maxValue / markDivision).toInt()
        val startY = height - padding
        val stopY = padding

        val axeYHeight = startY - stopY

        val scaleYMarkDist = axeYHeight / markDivision
        val textPaddingX = 30f
        var textPaddingY = startY - scaleYMarkDist + drawTextSetOff

        var newDiv = barDiv
        for (i in 0 until markDivision) {
            canvas.drawText(newDiv.toString(), textPaddingX, textPaddingY, scaleTextPaint)
            textPaddingY -= scaleYMarkDist
            newDiv += barDiv
        }

    }

    private fun drawValuesX(canvas: Canvas, markDivision: Int, drawTextSetOff: Float) {
        val barDiv = daysCount / markDivision

        val startX = padding
        val stopX = width - padding

        val axeXHeight = stopX - startX

        val scaleXMarkDist = axeXHeight / markDivision
        var txtPaddingX = padding + scaleXMarkDist - drawTextSetOff
        val txtPaddingY = height - padding / 2

        var newDiv = barDiv
        for (i in 0 until markDivision) {
            canvas.drawText(newDiv.toString(), txtPaddingX, txtPaddingY, scaleTextPaint)
            txtPaddingX += scaleXMarkDist
            newDiv += barDiv
        }
    }


    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        bundle.putParcelable("superState", super.onSaveInstanceState())
        bundle.putParcelableArrayList("segment", segmentList)

        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            segmentList = state.getParcelableArrayList<Segment>("segment") as ArrayList<Segment>
            super.onRestoreInstanceState(state.getParcelable("superState"))
        }
    }
}