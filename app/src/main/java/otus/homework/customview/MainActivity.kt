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
        mState = createState()
        pieChart.setValue(mState!!)

        val lineChartState = LineChartState.Dates(
            items = listOf(
                LineChartState.LineChartItem(
                    Calendar.getInstance().apply { set(2022, 10, 6) }, 40
                ),
                LineChartState.LineChartItem(
                    Calendar.getInstance().apply { set(2022, 10, 7) }, 70
                ),
                LineChartState.LineChartItem(
                    Calendar.getInstance().apply { set(2022, 10, 10) }, 20
                ),
                LineChartState.LineChartItem(
                    Calendar.getInstance().apply { set(2022, 10, 4) }, 100
                ),
                LineChartState.LineChartItem(
                    Calendar.getInstance().apply { set(2022, 10, 11) }, 35
                ),
                LineChartState.LineChartItem(
                    Calendar.getInstance().apply { set(2022, 10, 23) }, 40
                ),
                LineChartState.LineChartItem(
                    Calendar.getInstance().apply { set(2022, 10, 16) }, 77
                )
            ),
            color = generateHSVColor()
        )

        pieChart.setOnSectorSelectListener {
            lineChart.setValue(lineChartState.copy(color = it?.color?.toInt() ?: Color.TRANSPARENT))
        }
    }

    private fun generateHSVColor(): Int {
        val hue = Random.nextDouble(0.0, 360.0).toFloat()
        val saturation = Random.nextDouble(0.1, 0.6).toFloat()
        val value = 0.9f
        return Color.HSVToColor(floatArrayOf(hue, saturation, value))
    }

    private fun getPayload() = resources.openRawResource(R.raw.payload)
        .use {
            Json.decodeToSequence<Company>(
                stream = it,
                format = DecodeSequenceMode.ARRAY_WRAPPED
            ).toList()
        }

    private fun createState() = getPayload().map {
        PieChartState.ColorState(
            value = it.amount,
            color = generateHSVColor().toLong(),
            id = it.id.toString()
        )
    }.let {
        PieChartState(it)
    }

}