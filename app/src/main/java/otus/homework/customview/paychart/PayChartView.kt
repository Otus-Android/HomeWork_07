package otus.homework.customview.paychart

import android.content.Context
import android.graphics.Canvas
import android.graphics.RectF
import android.os.Parcelable
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.sqrt

class PayChartView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {

    private var rectF = RectF(0f, 0f, 0f, 0f)
    private val sectorList = mutableListOf<Sector>()

    var onSectorClickListener: ((id: Int) -> Unit)? = null
    private var clickedSectorId: Int? = null

    fun setPieces(set: Set<Piece>) {
        val sum = set.map { it.value }.sum()
        var startAngle = 0f
        sectorList.clear()
        PaintStore.reset()
        set.forEach { piece ->
            val percent = piece.value / sum
            val sweepAngle = 360 * percent
            val p = PaintStore.getPaint()
            sectorList.add(Sector(piece.id, startAngle, sweepAngle, p))
            startAngle += sweepAngle
        }
        requestLayout()
        invalidate()
    }

    override fun onSaveInstanceState(): Parcelable {
        val parcelable = super.onSaveInstanceState()
        return SavedState(sectorList, parcelable)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val ss = state as SavedState
        super.onRestoreInstanceState(ss.superState)
        sectorList.clear()
        ss.getSectorList()?.let { sectorList.addAll(it) }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val min = minOf(widthSize, heightSize)
        setMeasuredDimension(min, min)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val widthAddition = (width.toFloat() - measuredWidth) / 2
        val heightAddition = (height.toFloat() - measuredHeight) / 2
        rectF.let {
            it.left = widthAddition + paddingLeft
            it.top = heightAddition + paddingTop
            it.right = measuredWidth + widthAddition - paddingRight.toFloat()
            it.bottom = measuredHeight + heightAddition - paddingBottom.toFloat()
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        sectorList.forEach {
            canvas?.drawArc(rectF, it.startAngle, it.sweepAngle, true, it.paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return if (event != null && event.action == MotionEvent.ACTION_DOWN &&
            findTouchedSector(event.x, event.y)?.apply {
                clickedSectorId = this.id
                performClick()
            } != null) {
            true
        } else {
            super.onTouchEvent(event)
        }
    }

    override fun performClick(): Boolean {
        onSectorClickListener?.invoke(clickedSectorId!!)
        return super.performClick()
    }

    private fun findTouchedSector(touchX: Float, touchY: Float): Sector? {
        if (rectF.centerY() - rectF.top != rectF.right - rectF.centerX()) {
            throw UnsupportedOperationException("Graf must be circle!")
        }

        val centerX = rectF.centerX()
        val centerY = rectF.centerY()

        val normX = rectF.right
        val normY = rectF.centerY()

        val normVectorX = normX - centerX
        val normVectorY = normY - centerY

        val touchVectorX = touchX - centerX
        val touchVectorY = touchY - centerY

        val normVectorsModule = sqrt(normVectorX * normVectorX + normVectorY * normVectorY)
        val touchVectorsModule = sqrt(touchVectorX * touchVectorX + touchVectorY * touchVectorY)

        return if (touchVectorsModule < normVectorsModule){
            val vectorMult = normVectorX * touchVectorX + normVectorY * touchVectorY
            val cosValue = vectorMult / (normVectorsModule * touchVectorsModule)
            val angle = if (touchY < centerY) {
                360 - acos(cosValue) * 180 / PI
            } else {
                acos(cosValue) * 180 / PI
            }
            sectorList.first { it.startAngle < angle && angle < it.startAngle + it.sweepAngle }
        } else {
            null
        }
    }
}
