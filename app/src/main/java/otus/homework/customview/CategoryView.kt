package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class CategoryView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val rectangle by lazy { RectF() }
    private val paint by lazy { Paint() }

    private var labelText: String? = null
    private var overHead: Float? = null
    private var maxValue: Double = 0.0

    private var categoryWidth: Int = 0
    private var categoryHeight: Int = 0

    private var defaultBlockWidth = 30.px.toFloat()

    private var categories: List<Category> = emptyList()

    init {
        initAttributes(attrs)
        paint.apply {
            style = Paint.Style.FILL
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val wMode = MeasureSpec.getMode(widthMeasureSpec)
        val hMode = MeasureSpec.getMode(heightMeasureSpec)

        when (wMode) {
            MeasureSpec.AT_MOST, MeasureSpec.UNSPECIFIED -> {
                categoryWidth = (defaultBlockWidth * categories.size).toInt()
                categoryHeight = MeasureSpec.getSize(heightMeasureSpec)
            }

            MeasureSpec.EXACTLY -> {
                categoryWidth = MeasureSpec.getSize(widthMeasureSpec)
                categoryHeight = MeasureSpec.getSize(heightMeasureSpec)
            }
        }
        setMeasuredDimension(categoryWidth, categoryHeight)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val blockWidth = categoryWidth / categories.size
        val blockHeight = categoryHeight / maxValue

        var currentValueX = 0.0
        categories.forEach { category ->
            paint.apply { color = category.color }
            rectangle.set(
                currentValueX.toFloat(),
                (categoryHeight - blockHeight * category.value).toFloat(),
                (currentValueX + blockWidth).toFloat(),
                categoryHeight.toFloat()
            )
            canvas?.drawRect(rectangle, paint)
            currentValueX += blockWidth
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val bundle = state as Bundle
        val savedState = bundle.getString(SAVED_STATE_VIEW_KEY)
        savedState?.let {
            val viewState = Json.decodeFromString<SavedCategoryState>(it)
            this@CategoryView.categories = viewState.charts
        }
        super.onRestoreInstanceState(bundle.getParcelable(INSTANCE_CATEGORY_KEY))
    }

    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        bundle.putString(SAVED_STATE_VIEW_KEY, Json.encodeToString(SavedCategoryState(categories)))
        bundle.putParcelable(INSTANCE_CATEGORY_KEY, super.onSaveInstanceState())
        return bundle
    }

    private fun initAttributes(attrs: AttributeSet?) {
        context.withStyledAttributes(attrs, R.styleable.CategoryView, 0, 0) {
            labelText = getString(R.styleable.CategoryView_chart_label)
            overHead = getFloat(R.styleable.CategoryView_chart_sum_overhead, 0F)
        }
    }

    fun setCharts(charts: List<Category>) {
        this@CategoryView.categories = charts
        maxValue = charts.maxOf { it.value }
        requestLayout()
    }

    private companion object {
        private const val SAVED_STATE_VIEW_KEY = "saved_state_view_key"
        private const val INSTANCE_CATEGORY_KEY = "instance_category_key"
    }
}