package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.core.content.withStyledAttributes
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.math.atan2

class PieChart @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var pieces: List<PieData> = emptyList()
    private val rectangle by lazy { RectF(0F, 0F, pieWidth.toFloat(), pieHeight.toFloat()) }

    private val sumPaint by lazy {
        Paint().apply {
            textSize = 32F
            isAntiAlias = true
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
        }
    }
    private val circlePaint = Paint()
    private val centerPaint = Paint()

    private var labelText: String? = null
    private var labelDescription: String? = null
    private var pieHeight: Int = 0
    private var pieWidth: Int = 0

    init {
        initAttributes(attrs)
        centerPaint.apply {
            color = Color.WHITE
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val wMode = MeasureSpec.getMode(widthMeasureSpec)
        val hMode = MeasureSpec.getMode(heightMeasureSpec)
        when (wMode) {
            MeasureSpec.AT_MOST, MeasureSpec.UNSPECIFIED -> {
                pieWidth = MeasureSpec.getSize(widthMeasureSpec)
                pieHeight = MeasureSpec.getSize(heightMeasureSpec)
            }

            MeasureSpec.EXACTLY -> {
                if (MeasureSpec.getSize(widthMeasureSpec) > MeasureSpec.getSize(heightMeasureSpec)) {
                    pieWidth = MeasureSpec.getSize(widthMeasureSpec) / 2
                    pieHeight = MeasureSpec.getSize(heightMeasureSpec)

                } else {
                    pieWidth = MeasureSpec.getSize(widthMeasureSpec)
                    pieHeight = MeasureSpec.getSize(heightMeasureSpec) / 2
                }
            }

        }
        setMeasuredDimension(pieWidth, pieHeight)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        pieces.forEach { pie ->
            circlePaint.apply { color = pie.color }
            canvas?.drawArc(
                rectangle,
                pie.startAngle.toFloat(),
                pie.sweepAngle.toFloat(),
                USE_ARC_CENTER_FLAG, circlePaint
            )
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                val touchX = event.x
                val touchY = event.y
                val angle = getAngleFromTouch(pieWidth / 2, pieHeight / 2, touchX, touchY)
                val category = getCategory(angle)
                Toast.makeText(context, category, Toast.LENGTH_SHORT).show()
            }

            else -> {}
        }
        performClick()
        return super.onTouchEvent(event)
    }

    private fun getAngleFromTouch(centerX: Int, centerY: Int, x: Float, y: Float): Float {
        val dx = x - centerX
        val dy = y - centerY
        val angleInRadians = atan2(dy, dx).toDouble()
        val angleInDegrees = Math.toDegrees(angleInRadians).toFloat()
        return (angleInDegrees + 360f) % 360f
    }

    private fun getCategory(angle: Float): String {
        val category =
            pieces.find { angle > it.startAngle && angle < it.startAngle + it.sweepAngle }
        return category?.name ?: ""
    }

    override fun performClick(): Boolean = super.performClick()

    override fun onRestoreInstanceState(state: Parcelable?) {
        val bundle = state as Bundle
        val savedState = bundle.getString(SAVED_STATE_VIEW_KEY)
        savedState?.let {
            val viewState = Json.decodeFromString<SavedPiecesState>(it)
            this@PieChart.pieces = viewState.pieces
        }
        super.onRestoreInstanceState(bundle.getParcelable(INSTANCE_KEY))
    }

    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        bundle.putString(SAVED_STATE_VIEW_KEY, Json.encodeToString(SavedPiecesState(pieces)))
        bundle.putParcelable(INSTANCE_KEY, super.onSaveInstanceState())
        return bundle
    }

    private fun initAttributes(attrs: AttributeSet?) {
        context.withStyledAttributes(attrs, R.styleable.CustomPieChart, 0, 0) {
            labelText = getString(R.styleable.CustomPieChart_pie_label)
            labelDescription = getString(R.styleable.CustomPieChart_pie_description)
        }
    }

    fun setPieces(list: List<PieData>) {
        this@PieChart.pieces = list
        requestLayout()
    }

    private companion object {
        private const val SAVED_STATE_VIEW_KEY = "saved_state_view_key"
        private const val INSTANCE_KEY = "instance_key"
        private const val USE_ARC_CENTER_FLAG = true
    }
}