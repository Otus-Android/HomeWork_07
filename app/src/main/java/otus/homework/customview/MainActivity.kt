package otus.homework.customview

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.serialization.json.DecodeSequenceMode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeToSequence
import otus.homework.customview.lineChart.LineChartState
import otus.homework.customview.lineChart.LineChartView
import otus.homework.customview.pieChart.PieChartState
import otus.homework.customview.pieChart.PieChartView
import java.util.*
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private var mState: PieChartState? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val pieChart = findViewById<PieChartView>(R.id.pieChart)
        val lineChart = findViewById<LineChartView>(R.id.lineChart)
        mState = createPieChartState()
        if (savedInstanceState == null) {
            pieChart.setValue(mState!!)
        }

        pieChart.setOnSectorSelectListener { state ->
            lineChart.setValue(state?.let { getAmountByDay(state) } ?: LineChartState.default())
        }
    }

    private fun generateHSVColor(): Int {
        val hue = Random.nextDouble(0.0, 360.0).toFloat()
        val saturation = Random.nextDouble(0.1, 0.6).toFloat()
        val value = 0.9f
        return Color.HSVToColor(floatArrayOf(hue, saturation, value))
    }

    private fun getPayload() = resources.openRawResource(R.raw.payload)
        .use { stream ->
            Json.decodeToSequence<Company>(
                stream = stream,
                format = DecodeSequenceMode.ARRAY_WRAPPED
            ).toList()
        }

    private fun createPieChartState() = getPayload()
        .groupBy(Company::category)
        .mapValues { entry -> entry.value.sumOf { it.amount } }
        .map {
            PieChartState.ColorState(
                value = it.value,
                color = generateHSVColor().toLong(),
                id = it.key.id.toString()
            )
        }.let {
            PieChartState(it)
        }

    private fun getAmountByDay(pieChartColorState: PieChartState.ColorState): LineChartState.Dates {
        val items = getPayload().filter { it.category.id == pieChartColorState.id.toIntOrNull() }
            .map {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = it.time * 1000
                LineChartState.LineChartItem<Calendar>(
                    x = calendar,
                    y = it.amount
                )
            }.groupBy { it.x.get(Calendar.DAY_OF_MONTH) }
            .mapValues {
                LineChartState.LineChartItem(
                    x = it.value.first().x,
                    y = it.value.sumOf { it.y }
                )
            }
        return LineChartState.Dates(
            items = items.values.toList(),
            color = pieChartColorState.color
        )
    }

}