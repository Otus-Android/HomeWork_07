package otus.homework.customview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Region
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat.getColor

class PieChart @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {
    private val colors = listOf(
        getColor(context, R.color.abc),
        getColor(context, R.color.rigla),
        getColor(context, R.color.five),
        getColor(context, R.color.truffo),
        getColor(context, R.color.simple_wine),
        getColor(context, R.color.abc_express),
        getColor(context, R.color.uber),
        getColor(context, R.color.metro),
        getColor(context, R.color.dentist),
        getColor(context, R.color.five_2),
        getColor(context, R.color.pool),
        getColor(context, R.color.uber_2),
    )
    private var paths = listOf<Path>()
    private val paint = Paint()
    private var path = Path()

    private val rectF = RectF()
    private var centerX = 0f
    private var centerY = 0f

    private var playloads = listOf<Playload>()
    private var playloadsAmountSum = 0

    private var index = 0
    private var section = 0f
    private var start = 0f
    var touchListener : ((Playload) -> Unit)? = null

    fun setData(playloads: List<Playload>) {
        this.playloads = playloads
        paths = playloads.map { Path() }
        playloadsAmountSum = playloads.sumOf { it.amount }
    }

    fun setOnClickListenerItem(action: ((Playload) -> Unit)?) {
        touchListener = action
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        var width = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        var height = MeasureSpec.getSize(heightMeasureSpec)

        when (widthMode) {
            MeasureSpec.AT_MOST, MeasureSpec.UNSPECIFIED -> if (width > height) {
                width = height
            }
        }
        when (heightMode) {
            MeasureSpec.AT_MOST, MeasureSpec.UNSPECIFIED -> if (height > width) {
                height = width
            }
        }

        val horizontalOffset = if (height > width) 0f else (width - height).toFloat() / 2
        val verticalOffset = if (width > height) 0f else (height - width).toFloat() / 2

        rectF.left = horizontalOffset + paddingLeft
        rectF.top = verticalOffset + paddingTop
        rectF.right = width - horizontalOffset - paddingRight
        rectF.bottom = height - verticalOffset - paddingBottom

        centerX = width.toFloat() / 2
        centerY = height.toFloat() / 2

        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        playloads.forEach() { item ->
            index = item.id - 1
            section = (item.amount.toFloat() / playloadsAmountSum) * 360
            paint.color = colors[index % colors.size]
            paths[index].arcTo(
                rectF,
                start,
                section,
                true
            )
            paths[index].lineTo(centerX, centerY)
            paths[index].close()

            canvas.drawPath(paths[index], paint)
            start += section
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {

        if(event?.action  == MotionEvent.ACTION_DOWN){
            val rect = RectF()
            val region = Region()

            playloads.forEachIndexed { index, outlay ->
                paths[index].computeBounds(rect, false)
                region.setPath(paths[index], Region(rect.left.toInt(), rect.top.toInt(), rect.right.toInt(), rect.bottom.toInt())
                )
                if (region.contains(event.x.toInt(), event.y.toInt())) {
                    touchListener?.invoke(outlay)
                }
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onSaveInstanceState(): Parcelable {
        return PlayloadState(super.onSaveInstanceState()).apply {
            this.playloads = playloads
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is PlayloadState) {
            playloads = state.playloads.toMutableList()
        }
        super.onRestoreInstanceState(state)
    }
}
