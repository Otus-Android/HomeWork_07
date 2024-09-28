package otus.homework.customview

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.os.Parcelable
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.core.graphics.ColorUtils.HSLToColor
import kotlinx.parcelize.Parcelize
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.min

class PieChartView @JvmOverloads constructor (
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    private var mapPaint: MutableMap<String, Long> = mutableMapOf()
    private var maxCost: Long = -1
    private var totalCost: Long = -1
    private var selectedCategory: String = ""
    private var onCategoryListener: ((String) -> Unit)? = null

    private val minHeight = 300.px
    private val minWidth = 300.px
    private val offsetPie = 30f

    private val pieRect = RectF()
    private var piePaint: Paint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private var selectedPiePaint: Paint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.BLACK
        strokeWidth = 6f
        isAntiAlias = true
        pathEffect = DashPathEffect(
            floatArrayOf( 5f, 5f ),
            0f
        )
    }

    private val circlePaint = Paint().apply {
        color = Color.TRANSPARENT
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private var arcRect = RectF()

    fun setSelectionListener(onCategoryListener: (String) -> Unit) {
        this.onCategoryListener = onCategoryListener
    }

    fun setSelectedCategory( category: String) {
        selectedCategory = category
        onCategoryListener?.invoke(category)
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val minSize = min( w, h ) - offsetPie
        pieRect.set(offsetPie, offsetPie, minSize, minSize)
    }
    override fun onDraw(canvas: Canvas) {
        var littleAngle= 0f
        val varVisiblePartRadius = pieRect.width()/2f/3f
        val constVisiblePartRadius = pieRect.width()/2f/6f
        val minRadius = pieRect.width()/2f - varVisiblePartRadius - constVisiblePartRadius
        var index = 0
        mapPaint.forEach {
            val currentAngle = it.value.toFloat()*360f/totalCost
            val arcRadius = minRadius + constVisiblePartRadius +
                    varVisiblePartRadius * it.value.toFloat() / maxCost
            piePaint.setColor(
                HSLToColor( floatArrayOf(360f*index++/mapPaint.count(), 0.9f, 0.5f))
            )

            arcRect.set(
                pieRect.centerX() - arcRadius,
                pieRect.centerY() - arcRadius,
                pieRect.centerX() + arcRadius,
                pieRect.centerY() + arcRadius
            )
            canvas.drawArc(arcRect, littleAngle, currentAngle, true, piePaint )
            if(selectedCategory==it.key) {
                if ((resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK)
                    == Configuration.UI_MODE_NIGHT_YES )
                    selectedPiePaint.color = Color.WHITE
                else
                    selectedPiePaint.color = Color.BLACK
                canvas.drawArc(arcRect, littleAngle, currentAngle, true, selectedPiePaint)
            }
            littleAngle += currentAngle
        }
        canvas.drawCircle(pieRect.centerX(), pieRect.centerY(), minRadius, circlePaint )
    }

    private fun checkPoint(x: Float, y: Float): String {
        var category = ""
        var littleAngle= 0f
        val varVisiblePartRadius = pieRect.width()/2f/3f
        val constVisiblePartRadius = pieRect.width()/2f/6f
        val minRadius = pieRect.width()/2f - varVisiblePartRadius - constVisiblePartRadius
        val pointRadius = hypot(x-pieRect.centerX(), y-pieRect.centerY())
        if(pointRadius<minRadius) return category
        var anglePoint = 270f-180f / Math.PI *
                atan2(pieRect.centerX()-x, pieRect.centerY()-y).toDouble()
        if(anglePoint>360f) anglePoint -= 360f
        for (it in mapPaint) {
            val currentAngle = it.value.toFloat() * 360f / totalCost
            val arcRadius = minRadius + constVisiblePartRadius +
                    varVisiblePartRadius * it.value.toFloat() / maxCost
            if(anglePoint in littleAngle..littleAngle+currentAngle && pointRadius<=arcRadius) {
                category = it.key
                break
            }
            littleAngle += currentAngle
        }
        return category
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val hMode = MeasureSpec.getMode(heightMeasureSpec)
        val wMode = MeasureSpec.getMode(widthMeasureSpec)
        val wSize = MeasureSpec.getSize(widthMeasureSpec)
        val hSize = MeasureSpec.getSize(heightMeasureSpec)

        val minSize = min(wSize, hSize)

        val newWidth = when (wMode) {
            MeasureSpec.EXACTLY -> minSize
            MeasureSpec.AT_MOST -> min(minSize, wSize)
            else -> minWidth
        }
        val newHeight = when (hMode) {
            MeasureSpec.EXACTLY -> minSize
            MeasureSpec.AT_MOST -> min(minSize, hSize)
            else -> minHeight
        }
        setMeasuredDimension(newWidth, newHeight)
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if(event.action ==MotionEvent.ACTION_UP) {
            this.performClick()
        }
        return generalGestureDetector.onTouchEvent(event)
    }

    private val generalGestureDetector = GestureDetector(context, object : SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            val category = checkPoint(e.x, e.y)
            if(category.isNotEmpty()) {
                setSelectedCategory(category)
                Toast.makeText( context, category, Toast.LENGTH_SHORT).show()
            }
            return true
        }
    })

    fun setData(receipts: List<ReceiptData>) {
        mapPaint = receipts
            .groupBy { it.category }
            .mapValues {
                    (_, records) -> records.sumOf { it.amount }
            }.toMutableMap()
        maxCost = mapPaint.maxOf { it.value }
        totalCost = mapPaint.values.sum()
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        return PieChartState(superState, selectedCategory)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val restoreState = state as? PieChartState
        super.onRestoreInstanceState(restoreState?.superState ?: state)
        val restoreCategory = restoreState?.selectedCategory ?: ""
        setSelectedCategory( restoreCategory )
    }
}

@Parcelize
class PieChartState(private val superSavedState: Parcelable?, val selectedCategory: String) :
    View.BaseSavedState(superSavedState), Parcelable