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
import androidx.core.graphics.toRect
import androidx.core.graphics.toRegion

class CategoriesPieChart(context: Context, attributeSet: AttributeSet? = null) :
    View(context, attributeSet) {

    var listener: CategoryClickListener? = null

    private val paint = Paint().apply {
        strokeWidth = 10f
    }
    private val paintColors = listOf(
        Color.RED,
        Color.BLUE,
        Color.CYAN,
        Color.GREEN,
        Color.YELLOW,
        Color.GRAY,
        Color.MAGENTA,
        Color.DKGRAY,
        Color.BLACK,
        Color.LTGRAY
    )

    private var angleData = listOf<Slice>()
    private var rect = RectF()
    private var ringWidth = -1
    private var innerRect = RectF()
    private var region = Region()
    private val defaultSize = (100 * Resources.getSystem().displayMetrics.density).toInt()

    private var onDrawCompleted = false

    init {
        isSaveEnabled = true
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.CategoriesPieChart)
        try {
            ringWidth = typedArray.getDimensionPixelSize(R.styleable.CategoriesPieChart_ring_width, -1)
        } finally {
            typedArray.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        val size: Int
        when {
            widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY -> {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec)
                size = height.coerceAtMost(width)
            }
            (widthMode == MeasureSpec.AT_MOST || widthMode == MeasureSpec.UNSPECIFIED)
                    && (heightMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.UNSPECIFIED) -> {
                size = defaultSize
                setMeasuredDimension(size, size)
            }
            widthMode == MeasureSpec.EXACTLY -> {
                size = width
                setMeasuredDimension(size, size)
            }
            heightMode == MeasureSpec.EXACTLY -> {
                size = height
                setMeasuredDimension(size, size)
            }
            else -> {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec)
                size = height.coerceAtMost(width)
            }
        }

        val radius = size.toFloat() / 2
        val innerRadius = if (ringWidth >= 0) radius - radius.coerceAtMost(ringWidth.toFloat()) else radius * 2 / 3
        val midWidth = size / 2
        val midHeight = size / 2
        rect.set(midWidth - radius, midHeight - radius, midWidth + radius, midHeight + radius)
        innerRect.set(midWidth - innerRadius, midHeight - innerRadius, midWidth + innerRadius, midHeight + innerRadius)
        region = rect.toRegion()
    }

    override fun onDraw(canvas: Canvas?) {
        if (angleData.isEmpty() || canvas == null) return
        var currentAngle = 0f
        angleData.forEachIndexed { i, slice ->
            val p = slice.path
            p.reset()
            paint.color = paintColors[i % paintColors.size]
            p.arcTo(rect, currentAngle, slice.angle)
            p.arcTo(innerRect, currentAngle + slice.angle, -slice.angle)
            p.close()
            canvas.drawPath(p, paint)
            currentAngle += slice.angle
        }
        onDrawCompleted = true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (onDrawCompleted) {
            val r = Region()
            angleData.forEach {
                r.setPath(it.path, region)
                when (event.action) {
                    MotionEvent.ACTION_UP -> {
                        if (r.contains(event.x.toInt(), event.y.toInt())) listener?.onCategoryClick(it.name)
                    }
                }
            }
        }
        return true
    }

    fun setValues(items: Map<String, Float>) {
        val sum = items.values.sum()
        angleData = items.map { Slice(it.key, 360 * it.value / sum) }
        requestLayout()
        invalidate()
    }

    override fun onSaveInstanceState(): Parcelable? {
        val savedState = super.onSaveInstanceState()
        savedState?.let {
            return State(it).also { it.angleData = angleData }
        } ?: return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is State) {
            super.onRestoreInstanceState(state.superState)
            angleData = state.angleData.toMutableList()
            requestLayout()
            invalidate()
        } else super.onRestoreInstanceState(state)
    }

    class Slice(val name: String, val angle: Float) : Parcelable {
        val path = Path()

        constructor(parcel: Parcel) : this(
            parcel.readString().orEmpty(),
            parcel.readFloat()
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(name)
            parcel.writeFloat(angle)
        }

        override fun describeContents() = 0

        companion object CREATOR : Parcelable.Creator<Slice> {
            override fun createFromParcel(parcel: Parcel) = Slice(parcel)

            override fun newArray(size: Int): Array<Slice?> = arrayOfNulls(size)
        }
    }

    fun interface CategoryClickListener {
        fun onCategoryClick(name: String)
    }

    companion object {
        class State : BaseSavedState {

            var angleData = listOf<Slice>()

            constructor(parcelable: Parcelable) : super(parcelable)

            private constructor(parcel: Parcel) : super(parcel) {
                parcel.readList(angleData, Slice::class.java.classLoader)
            }

            override fun writeToParcel(parcel: Parcel, flags: Int) {
                parcel.writeList(angleData)
            }

            companion object CREATOR : Parcelable.Creator<State> {
                override fun createFromParcel(parcel: Parcel) = State(parcel)

                override fun newArray(size: Int): Array<State?> = arrayOfNulls(size)
            }
        }
    }
}