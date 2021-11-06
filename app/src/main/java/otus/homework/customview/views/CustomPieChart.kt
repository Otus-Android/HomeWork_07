package otus.homework.customview.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.graphics.toRegion
import otus.homework.customview.R

class CustomPieChart(context: Context, attributeSet: AttributeSet? = null) :
    View(context, attributeSet) {

    private val paint = Paint()
    private var rect = RectF()
    private var innerRect = RectF()
    private var region = Region()
    private var pieWidth = -1
    private var onDrawCompleted = false
    private var pieSliceData = listOf<PieSlice>()

    @RequiresApi(Build.VERSION_CODES.O)
    private val colorsList = listOf(
        Color.rgb(255f, (0..255).random().toFloat(), (0..255).random().toFloat()),
        Color.rgb(255f, (0..255).random().toFloat(), (0..255).random().toFloat()),
        Color.rgb(255f, (0..255).random().toFloat(), (0..255).random().toFloat()),
        Color.rgb(255f, (0..255).random().toFloat(), (0..255).random().toFloat()),
        Color.rgb(255f, (0..255).random().toFloat(), (0..255).random().toFloat()),
        Color.rgb(255f, (0..255).random().toFloat(), (0..255).random().toFloat()),
        Color.rgb(255f, (0..255).random().toFloat(), (0..255).random().toFloat()),
        Color.rgb(255f, (0..255).random().toFloat(), (0..255).random().toFloat()),
        Color.rgb(255f, (0..255).random().toFloat(), (0..255).random().toFloat()),
        Color.rgb(255f, (0..255).random().toFloat(), (0..255).random().toFloat()),
    )

    var listener: ClickListener? = null

    init {
        isSaveEnabled = true
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.CustomPieChart)

        try {
            pieWidth = typedArray.getDimensionPixelSize(R.styleable.CustomPieChart_pie_width, -1)
        } finally {
            typedArray.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        val size: Int

        when {
            widthMode == MeasureSpec.EXACTLY -> {
                size = width
                setMeasuredDimension(size, size)
            }

            heightMode == MeasureSpec.EXACTLY -> {
                size = height
                setMeasuredDimension(size, size)
            }

            (widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY) -> {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec)
                size = if (height <= width) height else width
            }

            (widthMode == MeasureSpec.AT_MOST || widthMode == MeasureSpec.UNSPECIFIED)
                    && (heightMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.UNSPECIFIED) -> {
                size = (300 * Resources.getSystem().displayMetrics.density).toInt()
                setMeasuredDimension(size, size)
            }

            else -> {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec)
                size = if (height <= width) height else width
            }
        }

        val radius = size.toFloat() / 2

        val innerRadius = if (pieWidth >= 0) {
            radius - if (radius <= pieWidth.toFloat()) radius else pieWidth.toFloat()
        } else {
            radius / 2
        }

        val midWidth = size / 2
        val midHeight = size / 2

        rect.set(midWidth - radius, midHeight - radius, midWidth + radius, midHeight + radius)
        innerRect.set(
            midWidth - innerRadius,
            midHeight - innerRadius,
            midWidth + innerRadius,
            midHeight + innerRadius
        )
        region = rect.toRegion()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDraw(canvas: Canvas?) {
        if (pieSliceData.isEmpty() || canvas == null) return

        var currentAngle = 0f

        pieSliceData.forEachIndexed { i, slice ->
            val p = slice.path
            p.reset()
            paint.color = colorsList[i % colorsList.size]
            p.arcTo(rect, currentAngle, slice.angle)
            p.arcTo(innerRect, currentAngle + slice.angle, -slice.angle)
            p.close()
            canvas.drawPath(p, paint)
            currentAngle += slice.angle
        }
        onDrawCompleted = true
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (onDrawCompleted) {
            val r = Region()
            pieSliceData.forEach {
                r.setPath(it.path, region)
                when (event.action) {
                    MotionEvent.ACTION_UP -> {
                        if (r.contains(event.x.toInt(), event.y.toInt())) {
                            listener?.onClick(it.name)
                            Log.i("!!!", "onTouchEvent: clicked")
                        }
                    }
                }
            }
        }
        return true
    }

    fun setValues(items: Map<String, Float>) {
        val sum = items.values.sum()
        pieSliceData = items.map { PieSlice(it.key, 360 * it.value / sum) }
        requestLayout()
        invalidate()
    }

    override fun onSaveInstanceState(): Parcelable? {
        val savedState = super.onSaveInstanceState()
        savedState?.let {
            return PieState(it).also { it.angleData = pieSliceData }
        } ?: return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is PieState) {
            super.onRestoreInstanceState(state.superState)
            pieSliceData = state.angleData.toMutableList()
            requestLayout()
            invalidate()
        } else super.onRestoreInstanceState(state)
    }

    fun interface ClickListener {
        fun onClick(name: String)
    }
}

class PieState(parcelable: Parcelable) : View.BaseSavedState(parcelable) {
    var angleData = listOf<PieSlice>()
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeList(angleData)
    }

}