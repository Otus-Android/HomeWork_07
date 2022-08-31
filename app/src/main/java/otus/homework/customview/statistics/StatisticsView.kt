package otus.homework.customview.statistics

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.CornerPathEffect
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorRes
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import otus.homework.customview.dp
import otus.homework.customview.generateRandomColor
import otus.homework.customview.piechart.SegmentsDataEntity
import java.util.*


class StatisticsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var currentState: CustomStatisticsViewState = CustomStatisticsViewState.NOT_INIZIALIZED

    private val xInterval: Float
        get() = (measuredWidth - 2 * axeOffset) / (currentState.countOfDays - 1)

    private val yInterval: Float
        get() = (measuredHeight - 2 * axeOffset) / (currentState.countOfYAxes - 1)

    private val amountInterval: Float
        get() = currentState.maxAmount / (currentState.countOfYAxes - 1f)

    private val sizeOfInterval = 2f.dp
    private val sizeOff = 2f.dp
    private val axePaint = Paint().apply {
        color = Color.LTGRAY
        strokeWidth = 1f.dp
        style = Paint.Style.FILL
        flags = Paint.ANTI_ALIAS_FLAG
        pathEffect = DashPathEffect(floatArrayOf(sizeOfInterval, sizeOff), 0f)
    }
    private val axeOffset = 50f.dp

    private val daysPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
        flags = Paint.ANTI_ALIAS_FLAG
        textSize = 5f.dp
        textAlign = Paint.Align.CENTER
    }
    private val daysTopMargin = 8f.dp

    private val sumPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
        flags = Paint.ANTI_ALIAS_FLAG
        textSize = 5f.dp
        textAlign = Paint.Align.LEFT
    }
    private val amountLeftMargin = 4f.dp

    private val calendar = Calendar.getInstance()

    fun setData(segmentsDataEntity: SegmentsDataEntity) {
        val countOfDays = 31
        val maxAmount = segmentsDataEntity.data.maxOf { it.amount }
        val trajectories = mutableListOf<Trajectory>()

        val groups = segmentsDataEntity.data.groupBy { it.category }.toMutableMap()
        groups.forEach { (key, value) ->
            groups[key] = value.sortedBy { it.time }
        }

        groups.values.forEach { items ->
            val points = mutableListOf<TrajectoryPoints>()
            points.add(TrajectoryPoints(1, 0))

            items.forEach {
                calendar.timeInMillis = it.time * 1000
                val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
                points.add(TrajectoryPoints(currentDay, it.amount))
            }
            trajectories.add(Trajectory(points = points))
        }

        currentState = CustomStatisticsViewState.INIZIALIZED(
            trajectories = trajectories,
            maxAmount = maxAmount,
            countOfDays = countOfDays,
            countOfYAxes = 10
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val measureSpecWidthMode = MeasureSpec.getMode(widthMeasureSpec)
        val measureSpecWidth = MeasureSpec.getSize(widthMeasureSpec)
        val measureSpecHeightMode = MeasureSpec.getMode(heightMeasureSpec)
        val measureSpecHeight = MeasureSpec.getSize(heightMeasureSpec)

        val currentWidth: Int = when (measureSpecWidthMode) {
            MeasureSpec.EXACTLY -> {
                measureSpecWidth
            }
            MeasureSpec.AT_MOST -> {
                measureSpecWidth
            }
            MeasureSpec.UNSPECIFIED -> {
                measureSpecWidth
            }
            else -> throw IllegalStateException("Incorrect Width")
        }

        val currentHeight: Int = when (measureSpecHeightMode) {
            MeasureSpec.EXACTLY -> {
                measureSpecHeight
            }
            MeasureSpec.AT_MOST -> {
                measureSpecHeight
            }
            MeasureSpec.UNSPECIFIED -> {
                measureSpecHeight
            }
            else -> throw IllegalStateException("Incorrect Height")
        }

        setMeasuredDimension(currentWidth, currentHeight)
    }

    override fun onDraw(canvas: Canvas) {
        drawGrid(canvas = canvas)

        currentState.trajectories.forEach { trajectory ->
            trajectory.onDraw(
                canvas = canvas,
                dayConvertor = ::convertDayInRealXAxe,
                amountConvertor = ::convertAmountInRealYAxe
            )
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        super.onSaveInstanceState()
        return BaseSavedState(currentState)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        currentState = (state as BaseSavedState).superState as CustomStatisticsViewState
        super.onRestoreInstanceState(state)
    }

    private fun drawGrid(canvas: Canvas) {
        drawXAxes(canvas)
        drawXAxeInfo(canvas)
        drawYAxes(canvas)
        drawYAxeInfo(canvas)
    }

    private fun drawXAxes(canvas: Canvas) {
        for (i in 0 until currentState.countOfDays) {
            canvas.drawLine(
                0f + axeOffset + (xInterval * i).toInt(),
                measuredHeight - axeOffset,
                0f + axeOffset + (xInterval * i).toInt(),
                0f + axeOffset,
                axePaint
            )
        }
    }

    private fun drawXAxeInfo(canvas: Canvas) {
        for (i in 0 until currentState.countOfDays) {
            canvas.drawText(
                "${i + 1}".padStart(2, '0'),
                0f + axeOffset + (xInterval * i).toInt(),
                measuredHeight - axeOffset + daysTopMargin,
                daysPaint
            )
        }

    }

    private fun drawYAxes(canvas: Canvas) {
        for (i in 0 until currentState.countOfYAxes) {
            canvas.drawLine(
                0f + axeOffset,
                measuredHeight - axeOffset - (yInterval * i).toInt(),
                measuredWidth - axeOffset,
                measuredHeight - axeOffset - (yInterval * i).toInt(),
                axePaint
            )
        }
    }

    private fun drawYAxeInfo(canvas: Canvas) {
        for (i in 0 until currentState.countOfYAxes) {
            canvas.drawText(
                (amountInterval * i).toInt().toString(),
                measuredWidth - axeOffset + amountLeftMargin,
                measuredHeight - axeOffset - (yInterval * i).toInt(),
                sumPaint
            )
        }
    }

    private fun convertDayInRealXAxe(day: Int): Float = axeOffset + (day - 1) * xInterval

    private fun convertAmountInRealYAxe(amount: Int): Float =
        measuredHeight - axeOffset - ((measuredHeight - 2 * axeOffset) / currentState.maxAmount) * amount

    @Parcelize
    private class TrajectoryPoints(
        val day: Int,
        val amount: Int
    ) : Parcelable

    @Parcelize
    private data class Trajectory(
        private val points: MutableList<TrajectoryPoints>,
        @ColorRes private val color: Int = generateRandomColor(),
    ) : Parcelable {

        @IgnoredOnParcel
        private val linePath = Path()

        @SuppressLint("ResourceAsColor")
        @IgnoredOnParcel
        private val linePaint = Paint().apply {
            color = this@Trajectory.color
            style = Paint.Style.STROKE
            flags = Paint.ANTI_ALIAS_FLAG
            strokeWidth = 2f.dp
            pathEffect = CornerPathEffect(24f.dp)
        }

        fun onDraw(
            canvas: Canvas,
            dayConvertor: Int.() -> Float,
            amountConvertor: Int.() -> Float
        ) {
            linePath.reset()


            linePath.moveTo(
                dayConvertor(points[0].day),
                amountConvertor(points[0].amount)
            )

            points.forEach { point ->
                linePath.lineTo(
                    dayConvertor(point.day),
                    amountConvertor(point.amount)
                )
            }

            canvas.drawPath(linePath, linePaint)
        }
    }

    private sealed class CustomStatisticsViewState(
        open val trajectories: List<Trajectory>,
        open val maxAmount: Int,
        open val countOfDays: Int,
        open val countOfYAxes: Int = 10,
    ) : Parcelable {

        @Parcelize
        object NOT_INIZIALIZED :
            CustomStatisticsViewState(mutableListOf(), 0, 0, 0)

        @Parcelize
        class INIZIALIZED(
            override val trajectories: List<Trajectory>,
            override val maxAmount: Int,
            override val countOfDays: Int,
            override val countOfYAxes: Int,
        ) : CustomStatisticsViewState(trajectories, maxAmount, countOfDays, countOfYAxes)
    }
}