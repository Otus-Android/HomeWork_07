package otus.homework.customview.presentation.pie.chart

import android.content.Context
import android.graphics.Canvas
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlinx.parcelize.Parcelize
import otus.homework.customview.presentation.line.chart.LineData
import otus.homework.customview.presentation.pie.chart.storages.CursorStorage
import otus.homework.customview.presentation.pie.chart.storages.PieAreaStorage
import otus.homework.customview.presentation.pie.chart.storages.PieDataStorage
import otus.homework.customview.presentation.pie.chart.storages.PiePaintStorage
import otus.homework.customview.presentation.pie.chart.storages.PiePaintStorage.Companion.DEFAULT_LABEL
import kotlin.math.max

/**
 * Круговой график
 */
class PieChartView constructor(
    context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int
) : View(context, attrs, defStyleAttr, defStyleRes) {

    constructor(context: Context) : this(context, null, 0, 0)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(
        context, attrs, defStyleAttr, 0
    )

    private val paintStorage = PiePaintStorage(resources)
    private val dataStorage = PieDataStorage()
    private val areaStorage = PieAreaStorage(paintStorage)
    private val cursorStorage = CursorStorage(areaStorage, dataStorage)

    /** Стиль отображения кругового графика */
    var style: PieStyle
        get() = paintStorage.style
        set(value) {
            paintStorage.style = value
            areaStorage.updateChart()
            invalidate()
        }

    /** Признак отображения отладочной информации */
    var isDebugModeEnabled: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    var sectorTapListener: PieSectorTapListener? = null


    /** Нарисовать круговой график по данным [LineData] */
    fun render(pieData: PieData) {
        dataStorage.update(pieData)
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        Log.d(
            TAG, "onMeasure:" +
                    "widthMeasureSpec = ${MeasureSpec.toString(widthMeasureSpec)}, " +
                    "heightMeasureSpec = ${MeasureSpec.toString(heightMeasureSpec)}"
        )

        val label = DEFAULT_LABEL
        paintStorage.label.getTextBounds(label, 0, label.length, paintStorage.labelRect)

        val viewMinWith = paintStorage.labelRect.width() + paddingLeft + paddingRight
        val viewMinHeight = paintStorage.labelRect.height() + paddingTop + paddingBottom
        val requestedWidth = max(viewMinWith, suggestedMinimumWidth)
        val requestedHeight = max(viewMinHeight, suggestedMinimumHeight)

        val requestedSize = max(requestedWidth, requestedHeight)

        val realWidth = resolveSizeAndState(requestedSize, widthMeasureSpec, 0)
        val realHeight = resolveSizeAndState(requestedSize, heightMeasureSpec, 0)

        setMeasuredDimension(realWidth, realHeight)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        Log.d(TAG, "onSizeChanged: w = $w, h = $h, oldw = $oldw, oldh = $oldh")
        areaStorage.update(
            width = w,
            height = h,
            leftPadding = paddingLeft,
            topPadding = paddingTop,
            rightPadding = paddingRight,
            bottomPadding = paddingBottom
        )
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (cursorStorage.update(event.x, event.y)) {
                    sectorTapListener?.onDown(cursorStorage.getNode()?.payload)
                    invalidate()
                }
                true
            }

            MotionEvent.ACTION_UP -> {
                sectorTapListener?.onUp(cursorStorage.getNode()?.payload)
                cursorStorage.clear()
                invalidate()
                true
            }

            MotionEvent.ACTION_CANCEL -> {
                sectorTapListener?.onUp(cursorStorage.getNode()?.payload)
                cursorStorage.clear()
                invalidate()
                false
            }

            else -> false
        }
    }

    override fun onDraw(canvas: Canvas) {
        drawSectors(canvas)
        drawCursor(canvas)
        drawLabel(canvas)

        if (isDebugModeEnabled) {
            drawDebugGrid(canvas)
            drawDebugAreas(canvas)
        }
    }

    /** Нарисовать сектора графика */
    private fun drawSectors(canvas: Canvas) {
        dataStorage.getNodes().takeIf { it.isNotEmpty() }?.forEach { node ->
            paintStorage.pie.color = node.color
            if (node.startAngle != cursorStorage.getNode()?.startAngle) {
                canvas.drawArc(
                    areaStorage.default,
                    node.startAngle,
                    node.sweepAngle,
                    paintStorage.style.isFilled,
                    paintStorage.pie
                )
            }
        }
    }

    /** Выделить сектор, соответствующий позиции курсора */
    private fun drawCursor(canvas: Canvas) {
        cursorStorage.getNode()?.let { node ->
            paintStorage.pie.color = node.color
            canvas.drawArc(
                areaStorage.expanded,
                node.startAngle + SELECTED_SECTOR_GAP_DEGREE,
                node.sweepAngle - 2 * SELECTED_SECTOR_GAP_DEGREE,
                paintStorage.style.isFilled,
                paintStorage.pie
            )
        }
    }


    /** Нарисовать подпись, соответствующую позиции курсора */
    private fun drawLabel(canvas: Canvas) {
        cursorStorage.getNode()?.label?.let { label ->
            paintStorage.label.getTextBounds(label, 0, label.length, paintStorage.labelRect)
            canvas.drawText(
                label,
                areaStorage.default.centerX() - paintStorage.labelRect.width() / 2,
                areaStorage.default.centerY() + paintStorage.labelRect.height() / 2,
                paintStorage.label
            )
        }
    }

    /** Нарисовать отладочную информацию по областям графика */
    private fun drawDebugAreas(canvas: Canvas) {
        canvas.drawRect(areaStorage.global, paintStorage.global)
        canvas.drawRect(areaStorage.padding, paintStorage.padding)
        canvas.drawRect(areaStorage.chart, paintStorage.chart)
        canvas.drawRect(areaStorage.default, paintStorage.default)
    }

    /** Нарисовать отладочную информацию по "сетке" */
    private fun drawDebugGrid(canvas: Canvas) {
        val cellCount = DEBUG_CELL_COUNT
        val area = areaStorage.chart
        val stepX = area.width() / cellCount
        val stepY = area.height() / cellCount
        var currentPointX = area.left
        var currentPointY = area.top
        for (i in 0 until cellCount) {
            canvas.drawLine(
                currentPointX, area.bottom, currentPointX, area.top, paintStorage.debugGrid
            )
            canvas.drawLine(
                area.left, currentPointY, area.right, currentPointY, paintStorage.debugGrid
            )
            currentPointX += stepX
            currentPointY += stepY
        }
    }

    override fun onSaveInstanceState(): Parcelable =
        SavedState(
            super.onSaveInstanceState(),
            dataStorage.origin,
            paintStorage.style,
            isDebugModeEnabled
        )

    override fun onRestoreInstanceState(state: Parcelable?) {
        super.onRestoreInstanceState(state)
        val savedState = state as SavedState
        style = savedState.style
        isDebugModeEnabled = savedState.isDebugModeEnabled
        render(savedState.pieData)
    }

    @Parcelize
    class SavedState(
        private val superSavedState: Parcelable?,
        val pieData: PieData,
        val style: PieStyle,
        val isDebugModeEnabled: Boolean,
    ) : BaseSavedState(superSavedState), Parcelable

    interface PieSectorTapListener {
        fun onDown(payload: Any?)

        fun onUp(payload: Any?)
    }

    private companion object {

        private const val TAG = "PieChartView"

        /** Кол-во клеток "сетки" отладочной информации */
        const val DEBUG_CELL_COUNT = 10

        /** Градус отступа выбранного сектора */
        const val SELECTED_SECTOR_GAP_DEGREE = 1
    }
}