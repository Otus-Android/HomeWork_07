package otus.homework.customview

import android.content.Context
import android.graphics.*
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.util.SparseArray
import android.view.MotionEvent
import android.view.View
import kotlinx.parcelize.Parcelize
import otus.homework.customview.data.Segment
import kotlin.math.*

class PieChart @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAtr: Int = 0
) : View(context, attrs, defStyleAtr) {

    private var segmentList: List<Segment> = listOf()
    private val biasMatrix = Matrix()

    private val pathList = List(12) { Path() }
    private var midX = 0F
    private var midY = 0F


    private val segmentPaint: Paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val corePaint: Paint = Paint().apply {
        isAntiAlias = true
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private val inscriptionPaint = Paint().apply {
        isAntiAlias = true
        color = Color.BLACK
        style = Paint.Style.FILL_AND_STROKE
        strokeWidth = 2F
        textSize = 40F
    }


    fun setData(segmentList: List<Segment>) {
        this.segmentList = segmentList.asSequence().sortedByDescending { it.value }.toList()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        when(widthMode) {
            MeasureSpec.AT_MOST -> {
                setMeasuredDimension(widthSize, heightSize)
            }
            MeasureSpec.EXACTLY -> {
                setMeasuredDimension(widthSize, heightSize)
            }
            MeasureSpec.UNSPECIFIED -> {
            }
        }

        when(heightMode) {
            MeasureSpec.AT_MOST -> {
                setMeasuredDimension(widthSize, heightSize)
            }
            MeasureSpec.EXACTLY -> {
                setMeasuredDimension(widthSize, heightSize)
            }
            MeasureSpec.UNSPECIFIED -> {

            }
        }
    }

    override fun onDraw(canvas: Canvas?) {
        if (canvas == null) return
        midX = width / 2F
        midY = height / 2F

        var startAngle = -90F
        var endAngel: Float
        var sweepAngle: Float

        val overallValue = segmentList.map { it.value }.sum()
        val radius = min(midX, midY) * 0.8F

        segmentList.forEachIndexed { index, currentSegment ->
            segmentPaint.color = segmentList[index].color

            endAngel = startAngle + (currentSegment.value/overallValue) * 360F
            sweepAngle = endAngel - startAngle

            if(isTouched && touchedAngle in startAngle..endAngel) {
                val angelOffSet = Math.toRadians((endAngel - sweepAngle / 2).toDouble())
                biasMatrix.setTranslate(
                    getTranslateX(angelOffSet),
                    getTranslateY(angelOffSet)
                )
                pathList[index].transform(biasMatrix)

                canvas.drawText(currentSegment.name, midX - radius , midY + radius + baseY + 25F, inscriptionPaint)
            } else {
                biasMatrix.reset()
                pathList[index].rewind()
                pathList[index].transform(biasMatrix)
                pathList[index].moveTo(midX, midY)
                pathList[index].arcTo(midX - radius, midY - radius, midX + radius, midY + radius, startAngle,
                    sweepAngle, false)
            }

            canvas.drawPath(pathList[index], segmentPaint)
            startAngle = endAngel
        }

        canvas.drawCircle(midX, midY, radius/3, corePaint)

    }

    // базовое смещение для матрицы
    private val baseX = 50F
    private val baseY = 50F

    /*
    расчет динамического смещения нашего сектора в зависимости от угла поворота
    направление смещения расчитывается согласно углу angelOffSet,
    которое считается от верхней части нашей окружности до середины нашего сектора
    */

    private fun getTranslateX(angelOffSet: Double): Float {
        return (baseX * cos(angelOffSet).toFloat())
    }
    private fun getTranslateY(angelOffSet: Double): Float {
        return (baseY * sin(angelOffSet)).toFloat()
    }


    private var isTouched = false
    private var touchedAngle = 0F



    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when(event?.action) {
            MotionEvent.ACTION_DOWN -> {
                calculateAngle(event.x, event.y)
                isTouched = true
                this.invalidate()
                return true
            }
            MotionEvent.ACTION_UP -> {
                isTouched = false
                invalidate()
                return true
            }
        }
        return false
    }

/*
Так как touchedAngle при пересчете отсчитывается, начиная с правой стороны:
 по часовой: 0 -> 90 -> 180;
 против часовой 0 -> -90 -> -180
 а канвас начинает отсчет, начиная справой стороны от -90 до 270 градусов,
 то вносим условие для пересчета touchedAngle на канвас
 */
    private fun calculateAngle(x: Float, y: Float) {
        touchedAngle = Math.toDegrees(atan2((y - midY).toDouble(), (x - midX).toDouble())).toFloat()
        Log.i("aaaaa", touchedAngle.toString())
        if (touchedAngle in -180F..-90F) {
            touchedAngle = 360F - abs(touchedAngle)
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        return CustomSavedState(superState)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state != null) {
            val customState = state as CustomSavedState
            super.onRestoreInstanceState(customState)
        }
    }

    @Parcelize
    internal class CustomSavedState(private val savedState: Parcelable?): BaseSavedState(savedState)
}