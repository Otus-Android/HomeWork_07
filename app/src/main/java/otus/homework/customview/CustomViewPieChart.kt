package otus.homework.customview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.RectF
import android.graphics.Region
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.toRegion
import kotlinx.coroutines.flow.MutableStateFlow
import android.os.Parcelable
import android.util.Log


private const val TAG = "debug"

class CustomViewPieChart(context: Context, attributeSet: AttributeSet) :
    View(context, attributeSet) {

    init {
        isSaveEnabled = true
    }

    private val defaultWidth = resources.getDimension(R.dimen.pie_chart).toInt()
    private val defaultHeight = resources.getDimension(R.dimen.pie_chart).toInt()
    private var stateView = ListPayment(mutableListOf(), null)
    private var userText = String()
    val pieChartFlow = MutableStateFlow("")
    private val userPaint = UserPaint()
    private var rectF = RectF()
    private var innerRectF = RectF()
    private var region = Region()

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

        rectF.set(
            width.toFloat() * 0.1f, height.toFloat() * 0.1f,
            width.toFloat() * 0.9f, height.toFloat() * 0.9f
        )
        innerRectF.set(
            width.toFloat() * 0.15f, height.toFloat() * 0.15f,
            width.toFloat() * 0.85f, height.toFloat() * 0.85f
        )
        region = rectF.toRegion()
    }

    override fun onDraw(canvas: Canvas) {
        if (stateView.listPayment.isEmpty()) return
        var arc = 0f
        stateView.listPayment.forEachIndexed { i, item ->
            val path = item.path
            path.reset()
            path.arcTo(rectF, arc, item.arc)
            path.arcTo(innerRectF, arc + item.arc, -item.arc)
            path.close()
            canvas.drawPath(path, userPaint.color[i].apply {
                isAntiAlias = true
            })
            arc += item.arc
        }

        // text amount sum
        if (userText.isEmpty()) {
            userText = "Total: ${stateView.listPayment.sumOf { it.amountSum }}"
        }
        canvas.drawText(
            userText,
            (width / 2).toFloat(),
            (height / 2).toFloat(),
            userPaint.blackPaint
        )
        requestLayout()
        invalidate()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val regionEvent = Region()
        if (event.action == MotionEvent.ACTION_DOWN) {
            stateView.listPayment.forEach {
                regionEvent.setPath(it.path, region)
                if (regionEvent.contains(event.x.toInt(), event.y.toInt())) {
                    userText = "${it.category}: ${it.amountSum}"
                    pieChartFlow.value = it.category
                }
            }
        }
        invalidate()
        return true
    }

    fun setValue(value: List<PaymentPieChart>) {
        stateView.listPayment.clear()
        stateView.listPayment.addAll(value)
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        return ListPayment(stateView.listPayment, superState)
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val listPaymentRestore = state as? ListPayment
        if (listPaymentRestore != null) {
            super.onRestoreInstanceState(listPaymentRestore.superSaveState)
            stateView.listPayment = listPaymentRestore.listPayment
        }
    }
}

