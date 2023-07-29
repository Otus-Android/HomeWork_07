package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import kotlin.math.*

class PieChart @JvmOverloads constructor(   // JvmOverloads создает несколько конструкторов,
                                            // если параметр не передали, берет по умолчанию
    context: Context,
    attr: AttributeSet? = null,
)
    : View(context,attr) {

        private val rectangle = RectF()
        private val upper = 25f
        private val defaultViewSize = 300
        private var index = -1
        private val viewStateTag = "index"
        private var expensesList = emptyList<Expenses>()
        private var sum = 1

    private val paintInner =  Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }
    private val paintText =  Paint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL
        textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 20f, resources.displayMetrics)
    }

    private val paintList =
        listOf(
            ContextCompat.getColor(context, R.color.pie_0),
            ContextCompat.getColor(context, R.color.pie_1),
            ContextCompat.getColor(context, R.color.pie_2),
            ContextCompat.getColor(context, R.color.pie_3),
            ContextCompat.getColor(context, R.color.pie_4),
            ContextCompat.getColor(context, R.color.pie_5),
            ContextCompat.getColor(context, R.color.pie_6),
            ContextCompat.getColor(context, R.color.pie_7),
            ContextCompat.getColor(context, R.color.pie_8),
            ContextCompat.getColor(context, R.color.pie_9)
        ).map { paintColor ->
            Paint().apply {
                color = paintColor
                style = Paint.Style.FILL
            }
        }

    private var partByCategories = expensesList
                            .groupBy { it.category }
                            .mapValues { entry -> ((entry.value.sumBy { it.amount})*360).toFloat()/sum }
                            .toList()

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            val widthSize = calculateSize(widthMeasureSpec)
            rectangle.top = 0f
            rectangle.bottom = widthSize.toFloat()
            rectangle.left = 0f
            rectangle.right = widthSize.toFloat()
            setMeasuredDimension(widthSize,widthSize)
        }

    private fun calculateSize(measureSpec: Int): Int {
        val size = MeasureSpec.getSize(measureSpec)
        return when (MeasureSpec.getMode(measureSpec)) {
            MeasureSpec.UNSPECIFIED -> defaultViewSize // например, наш вью будет в скролле
            MeasureSpec.EXACTLY -> size // например, когда задан android:layout_width =200dp или match_parent
            MeasureSpec.AT_MOST -> max(defaultViewSize, size) // например, когда задан layout_width="wrap_content"
            else -> throw IllegalStateException("Wrong MeasureSpec")
        }
    }

        override fun onDraw(canvas: Canvas?) {
            super.onDraw(canvas)
            drawSlices(canvas)
            drawCentralCircle(canvas)
            drawText(canvas)
        }

    private fun drawSlices(canvas: Canvas?) {
        var start = 0f
        for ((i, s) in partByCategories.withIndex()){
            if (i == index-1) {
                canvas?.drawArc(rectangle,start,s.second,true,paintList[i])
            }
            canvas?.drawArc(rectangle.left + upper,
                rectangle.top + upper,
                rectangle.right - upper,
                rectangle.bottom - upper,
                start,s.second,true,paintList[i])
            start+= s.second
        }
    }

    private fun drawCentralCircle(canvas: Canvas?) {
        canvas?.drawCircle((rectangle.top +rectangle.bottom)/2,
            (rectangle.left +rectangle.right)/2,
            (rectangle.right - rectangle.left)/3,paintInner)
    }

    private fun drawText(canvas: Canvas?) {
        if(index > 0) {
            val textWidth = paintText.measureText(partByCategories[index-1].first)
            canvas?.drawText(
                partByCategories[index-1].first,
                (rectangle.left + rectangle.right - textWidth) / 2,
                (rectangle.top + rectangle.bottom) / 2,
                paintText
            )
        } else {
            val textWidth = paintText.measureText(sum.toString())
            canvas?.drawText(
                sum.toString(),
                (rectangle.left + rectangle.right - textWidth) / 2,
                (rectangle.top + rectangle.bottom) / 2,
                paintText
            )
        }
    }

    //вызывается перед уничтожением View, и в нем необходимо сохранить состояние
    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        bundle.putInt(viewStateTag, index)
        bundle.putParcelable("instanceState", super.onSaveInstanceState())
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val bundle = state as Bundle
        index= bundle.getInt(viewStateTag)
        super.onRestoreInstanceState(bundle.getParcelable("instanceState"))
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.actionMasked == MotionEvent.ACTION_UP) {
            setIndex(event.x, event.y)
            invalidate()
            requestLayout()
        }
        return true
    }

    private fun setIndex(x: Float, y: Float) {
        var start = 0f
        val centerX = (rectangle.left + rectangle.right) / 2f
        val centerY = (rectangle.bottom + rectangle.top) / 2f
        val angle = getTouchAngle(x,y,centerX,centerY)
        for ((i, s) in partByCategories.withIndex()){
            if(angle==null) {
                index = -1
            } else if(angle in start..start+s.second) {
                index = if (index!= i +1)  i+1   else -1
            }
            start+= s.second
        }
    }

    private fun getTouchAngle(touchX: Float, touchY: Float, centerX: Float, centerY: Float): Float?{
        if(!isSlice(touchX,touchY)) return null
        return Math.toDegrees(
            atan2(
                (centerY - touchY).toDouble(),
                (centerX - touchX).toDouble()
            )
        ).toFloat()+180
    }

    private fun isSlice(touchX: Float, touchY: Float): Boolean {
        val outerRadius = (rectangle.bottom - rectangle.top)/2f
        val innerRadius = outerRadius/1.5f
        val centerX = (rectangle.left + rectangle.right)/2f
        val centerY = (rectangle.bottom + rectangle.top)/2f
        val realRadius = sqrt((touchX-centerX).pow(2) + (touchY-centerY).pow(2))
        return (realRadius in innerRadius..outerRadius)
    }

    fun setExpenses (expenses: List<Expenses>) {
        expensesList = expenses
        sum = if (expensesList.sumOf { it.amount }>0) expensesList.sumOf { it.amount } else 1
        partByCategories = expensesList
            .groupBy { it.category }
            .mapValues { entry -> ((entry.value.sumBy { it.amount})*360).toFloat()/sum }
            .toList()
    }
 }

