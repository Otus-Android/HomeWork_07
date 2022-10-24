package otus.homework.customview.entities

import android.graphics.RectF
import otus.homework.customview.tools.Time
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.log10
import kotlin.math.pow
import kotlin.properties.Delegates

class TimelineGrid(
    sourceList: List<Spending>,
    private val timeMapper: Time,
    private val rectForDraw: RectF
) {

    val amountLines: MutableList<AmountLine> = mutableListOf()
    val timeLines: MutableList<TimeLine> = mutableListOf()
    var timeInPixels by Delegates.notNull<Float>()
    var amountInPixels by Delegates.notNull<Float>()

    var minTime: Int = 0
    var maxTime: Int = 0
    var minAmount: Int = 0
    var maxAmount: Int = 0

    init {
        initExtremeValues(sourceList)
        initAmountGridLines()
        initTimeGridLines()
    }

    private fun initExtremeValues(list: List<Spending>) {
        minTime = list[0].time
        maxTime = list[0].time
        minAmount = list[0].amount
        maxAmount = list[0].amount
        list.forEach {
            if (it.time < minTime) minTime = it.time
            if (it.time > maxTime) maxTime = it.time
            if (it.amount < minAmount) minAmount = it.amount
            if (it.amount > maxAmount) maxAmount = it.amount
        }
    }

    private fun initAmountGridLines() {
        val verticalDesiredStep = maxAmount / LINES_NUMBER
        val baseVerticalGridStep = 10f.pow(verticalDesiredStep.length() - 1)
        val verticalGridStep =
            ceil((verticalDesiredStep / baseVerticalGridStep)) * baseVerticalGridStep
        for (i in 0..LINES_NUMBER) {
            amountLines
                .add(
                    AmountLine(
                        y = rectForDraw.bottom - i * (rectForDraw.height()) / LINES_NUMBER,
                        text = (i * verticalGridStep.toInt()).toString()
                    )
                )
        }
        amountInPixels =
            (rectForDraw.height()) / (verticalGridStep * LINES_NUMBER)
    }

    private fun initTimeGridLines() {
        val minDayInSeconds = timeMapper.timeToDateInSeconds(minTime)
        val maxDayInSeconds = timeMapper.timeToDateInSeconds(maxTime)
        val daysNumber = (maxDayInSeconds - minDayInSeconds) / timeMapper.secondsInDay()
        val horizontalGridStepInDays = ceil(daysNumber.toFloat() / TIME_CELLS_NUMBER)
        val numberVerticalLines = ceil(daysNumber / horizontalGridStepInDays).toInt()
        for (i in 0..numberVerticalLines) {
            timeLines.add(
                TimeLine(
                    x = rectForDraw.left + i * (rectForDraw.width()) / numberVerticalLines,
                    text = timeMapper.timeToDayAndMonthString(minTime + i * timeMapper.secondsInDay() * horizontalGridStepInDays.toInt())
                )
            )
        }
        timeInPixels =
            (rectForDraw.width()) / (horizontalGridStepInDays * numberVerticalLines * timeMapper.secondsInDay())
    }


    private fun Int.length() = when (this) {
        0 -> 1
        else -> log10(abs(toDouble())).toInt() + 1
    }

    companion object {
        private const val LINES_NUMBER = 5
        private const val TIME_CELLS_NUMBER = 5
    }

}