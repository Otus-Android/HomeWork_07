package otus.homework.customview

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toRegion

class PieChartView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {

    lateinit var pieChartViewModel: PieChartViewModel
    var paints: MutableList<Paint> = mutableListOf()
    private val colors = listOf(
        Color.MAGENTA,
        Color.BLUE,
        Color.CYAN,
        Color.GREEN,
        Color.GRAY,
        Color.RED,
        Color.YELLOW,
        Color.LTGRAY,
        Color.BLACK,
        Color.DKGRAY
    )

    var listCat: MutableList<PieCategory> = mutableListOf()
    private var box = RectF()
    val offset = 0F
    var region = Region()

    init {
        colors.forEachIndexed { index, element ->
            paints.add(index, Paint().apply {
                color = element
                textSize = 30F
            })
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val size: Int

        when {
            widthMode == MeasureSpec.EXACTLY -> {
                size = widthSize
                box = RectF(0F, offset, size.toFloat(), size.toFloat() + offset)
                setMeasuredDimension(size, size + offset.toInt())
            }

            heightMode == MeasureSpec.EXACTLY -> {
                size = heightSize
                box = RectF(0F, offset, size.toFloat() + offset, size.toFloat())
                setMeasuredDimension(size + offset.toInt(), size)
            }

            (widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY) -> {
                box = RectF(0F, offset, heightSize.toFloat(), widthSize.toFloat())
                super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            }

            (widthMode == MeasureSpec.AT_MOST || widthMode == MeasureSpec.UNSPECIFIED)
                && (heightMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.UNSPECIFIED) -> {
                size = (300 * Resources.getSystem().displayMetrics.density).toInt()
                setMeasuredDimension(size, size)
            }

            else -> {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            }
        }
        region = box.toRegion()
    }

    override fun onDraw(canvas: Canvas) {
        var start = 0F
        listCat.forEachIndexed { index, element ->
            element.path = Path()
            element.path.addArc(box, start, element.angle)
            element.path.lineTo(box.centerX(), box.centerY())
            canvas.drawPath(element.path, paints[index % paints.size])
            Log.d(
                "iszx",
                "start=$start angle=" + element.angle + " color=" + paints[index % paints.size]
            )
            element.category?.let {
                canvas.drawText(it, box.left, box.top + paints[0].textSize + (paints[0].textSize+10)*index, paints[index % paints.size])
            }
            start += element.angle
        }
    }

    fun onInit() {
        pieChartViewModel.onInit()
        pieChartViewModel.payloads.observe(context as AppCompatActivity, { payloads ->
            listCat.clear()
            val sum = payloads.values.sum()
            listCat = payloads.map {
                PieCategory(it.key, (360.0f / sum) * it.value)
            }.toMutableList()
        })
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_UP -> {
                val category = getCategory(event.x, event.y)
                Log.d("iszx", "category=$category")
            }
        }
        return true
    }

    private fun getCategory(x: Float, y: Float): String {
        if (box.contains(x, y)) {
            Log.d("iszx", "x=$x, y=$y")
            val r = Region()
            listCat.forEach {
                r.setPath(it.path, region)
                if (r.contains(x.toInt(), y.toInt())) {
                    return it.category ?: ""
                }
            }
        }
        return ""
    }

    override fun onSaveInstanceState(): Parcelable? {
        val savedState = super.onSaveInstanceState()
        savedState?.let { state ->
            return PieState(state).also { it.catData = listCat }
        } ?: return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is PieState) {
            super.onRestoreInstanceState(state.superState)
            listCat = state.catData.toMutableList()
            requestLayout()
            invalidate()
        } else super.onRestoreInstanceState(state)
    }

    class PieState(parcelable: Parcelable) : View.BaseSavedState(parcelable) {
        var catData = listOf<PieCategory>()
        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeList(catData)
        }
    }
}