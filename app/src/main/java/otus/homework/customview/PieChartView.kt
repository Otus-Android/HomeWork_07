package otus.homework.customview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.atan2
import kotlin.math.min

class PieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private var categories: List<Category> = emptyList()
    private var sum: Int = 0
    private var categoryClickListener: ((categoryName: String) -> Unit)? = null
    private var rect: RectF = RectF()
    private var centerX: Float = 0f
    private var centerY: Float = 0f
    private var radius: Float = 0f
    private var selectedIndex: Int = -1

    private val paint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 1f
        style = Paint.Style.FILL
    }

    private val selectedPaint = Paint().apply {
        style = Paint.Style.FILL
        shader = createSelectedShader()
    }

    private val colors = listOf(
        Color.parseColor("#73C5C5"),
        Color.parseColor("#F6D173"),
        Color.parseColor("#519DE9"),
        Color.parseColor("#EF9234"),
        Color.parseColor("#7CC674"),
        Color.parseColor("#B8BBBE"),
        Color.parseColor("#A30000"),
        Color.parseColor("#836394"),
        Color.parseColor("#38812F"),
        Color.parseColor("#8481DD"),
    )

    init {
        if (isInEditMode) {
            setup(
                listOf(
                    Category("1", 300),
                    Category("2", 400),
                    Category("3", 200),
                    Category("4", 100),
                )
            )
        }
    }

    fun setup(data: List<Category>) {
        categories = data
        sum = data.sumOf { it.amount }
    }

    fun setOnCategoryClickListener(listener: ((categoryName: String) -> Unit)?) {
        categoryClickListener = listener
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val defaultSize = 200
        val wMode = MeasureSpec.getMode(widthMeasureSpec)
        val hMode = MeasureSpec.getMode(heightMeasureSpec)
        val wSize = MeasureSpec.getSize(widthMeasureSpec)
        val hSize = MeasureSpec.getSize(heightMeasureSpec)

        val width = when (wMode) {
            MeasureSpec.EXACTLY -> wSize
            MeasureSpec.AT_MOST -> min(defaultSize, wSize)
            else -> defaultSize
        }
        val height = when (hMode) {
            MeasureSpec.EXACTLY -> hSize
            MeasureSpec.AT_MOST -> min(defaultSize, hSize)
            else -> defaultSize
        }

        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = w / 2f
        centerY = h / 2f
        radius = min(w, h) / 2f
        rect = RectF(
            centerX - radius + paddingLeft,
            centerY - radius + paddingTop,
            centerX + radius - paddingRight,
            centerY + radius - paddingBottom
        )
    }

    override fun onSaveInstanceState(): Parcelable =
        Bundle().apply {
            putParcelable(SUPER_STATE_KEY, super.onSaveInstanceState())
            putInt(SELECTED_INDEX_STATE_KEY, selectedIndex)
        }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val bundle = state as Bundle
        selectedIndex = bundle.getInt(SELECTED_INDEX_STATE_KEY, -1)
        super.onRestoreInstanceState(bundle.getParcelable(SUPER_STATE_KEY))
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (categories.isEmpty()) return

        var startAngle = 0f
        categories.forEachIndexed { index, category ->
            val angle = (360f / sum) * category.amount
            paint.color = colors[index % colors.size]
            canvas.drawArc(rect, startAngle, angle, true, paint)
            if (index == selectedIndex) {
                canvas.drawArc(rect, startAngle, angle, true, selectedPaint)
            }
            startAngle += angle
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_UP -> {
                if (isTouchInsidePie(event.x, event.y)) {
                    onPieClick(event.x, event.y)
                }
            }
        }
        return true
    }

    private fun isTouchInsidePie(x: Float, y: Float): Boolean {
        val distanceX = x - centerX
        val distanceY = y - centerY
        return (distanceX * distanceX) + (distanceY * distanceY) <= radius * radius
    }

    private fun onPieClick(x: Float, y: Float) {
        val touchDegree = getTouchDegree(x, y).toFloat()
        var startAngle = 0f
        categories.forEachIndexed { index, category ->
            val angle = (360f / sum) * category.amount
            if (touchDegree in startAngle..(startAngle + angle)) {
                categoryClickListener?.invoke(categories[index].name)
                selectedIndex = index
                invalidate()
                return
            }
            startAngle += angle
        }
    }

    private fun getTouchDegree(x: Float, y: Float) : Double {
        val diffX = x - centerX
        val diffY = y - centerY
        val radians = atan2(diffY, diffX).toDouble()
        val degrees = Math.toDegrees(radians)
        return if (degrees >= 0) degrees else 360 + degrees
    }

    private fun createSelectedShader(): Shader {
        val bitmap = Bitmap.createBitmap(20, 20, Bitmap.Config.ARGB_8888)
        val paint = Paint().apply {
            color = Color.BLACK
            strokeWidth = 2f
            style = Paint.Style.FILL
        }
        Canvas(bitmap).apply {
            drawColor(Color.TRANSPARENT)
            drawLine(0f, 0f, 20f, 20f, paint)
            drawLine(0f, 20f, 20f, 0f, paint)
        }
        return BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
    }

    data class Category(
        val name: String,
        val amount: Int,
    )

    companion object {
        private const val SELECTED_INDEX_STATE_KEY = "SELECTED_INDEX_STATE_KEY"
        private const val SUPER_STATE_KEY = "SUPER_STATE_KEY"
    }
}