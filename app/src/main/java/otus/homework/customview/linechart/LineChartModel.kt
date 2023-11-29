package otus.homework.customview

import androidx.lifecycle.ViewModel

class LineChartModel : ViewModel() {

    var grafData: Map<Long, Int>
    var maxAmount = 0
    var maxTime = 0L
    var minTime = 0L

    init {

        grafData = MainActivity.myData
            .groupBy { it.time }
            .mapValues {
                it.value.map { it.amount }
                    .fold(0) { summ, time -> summ + time }
            }
        maxAmount = grafData.maxOf { it.value }
        maxTime = grafData.maxOf { it.key }
        minTime = grafData.minOf { it.key }
    }
}
