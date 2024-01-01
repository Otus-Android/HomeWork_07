package otus.homework.customview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat.getColor

class PieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes)  {

    private val sectionColors = arrayOf(
        getColor(context, R.color.purple_100),
        getColor(context, R.color.purple_200),
        getColor(context, R.color.purple_300),
        getColor(context, R.color.purple_500),
        getColor(context, R.color.purple_700),
        getColor(context, R.color.purple_900),
        getColor(context, R.color.teal_100),
        getColor(context, R.color.teal_200),
        getColor(context, R.color.teal_500),
        getColor(context, R.color.teal_700),
        getColor(context, R.color.teal_900),
        getColor(context, R.color.teal_1100),
    )

    private var categories = mutableListOf<Category>()
    private var pathsSections = listOf<Path>()
    private var categoriesAmount = categories.size
    private val categoryRect = RectF()

    private var centerX = 0f
    private var centerY = 0f

    private var startAngle = -90f
    private var sectionAngle = 0f
    private val circleAngle = 360

    private var horizontalOffset = 0f
    private var verticalOffset = 0f

    var touchListener : ((Category) -> Unit)? = null

    private val paint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    fun setData(categoriesList: Set<Category>){

        categoriesAmount = categoriesList.sumOf { it.amount }
        categories.addAll(categoriesList)
        pathsSections = categories.map { Path() }

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
                width = height
            }
        }
        when (heightMode) {
            MeasureSpec.AT_MOST, MeasureSpec.UNSPECIFIED -> if (height > width) {
                height = width
            }
        }

        horizontalOffset = if (height > width) 0f else (width - height).toFloat() / 2
        verticalOffset = if (width > height) 0f else (height - width).toFloat() / 2

        categoryRect.left = horizontalOffset + paddingLeft
        categoryRect.top = verticalOffset + paddingTop
        categoryRect.right = width - horizontalOffset - paddingRight
        categoryRect.bottom = height - verticalOffset - paddingBottom

        centerX = width.toFloat() / 2
        centerY = height.toFloat() / 2

        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {

        categories.forEachIndexed { index, item ->
            sectionAngle = (item.amount.toFloat() / categoriesAmount) * circleAngle
            paint.color = sectionColors[index % 10]
            pathsSections[index].arcTo(
                categoryRect,
                startAngle,
                sectionAngle,
                true
            )
            pathsSections[index].lineTo(centerX, centerY)
            pathsSections[index].close()

            canvas.drawPath(pathsSections[index], paint)
            startAngle += sectionAngle
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        return CategoryState(super.onSaveInstanceState()).apply {
            this.categoryList = categories
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is CategoryState) {
            categories = state.categoryList.toMutableList()
        }
        super.onRestoreInstanceState(state)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {

        if(event?.action  == MotionEvent.ACTION_DOWN){
            handleEvent(event)
        }
        return super.onTouchEvent(event)
    }

    private fun handleEvent(event: MotionEvent){

        val rect = RectF()
        val region = Region()

        categories.forEachIndexed { index, outlay ->
            pathsSections[index].computeBounds(rect, false)
            region.setPath(pathsSections[index], Region(rect.left.toInt(), rect.top.toInt(), rect.right.toInt(), rect.bottom.toInt())
            )
            if (region.contains(event.x.toInt(), event.y.toInt())) {
                touchListener?.invoke(outlay)
            }
        }
    }
}