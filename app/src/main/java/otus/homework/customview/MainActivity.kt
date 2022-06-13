package otus.homework.customview

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.serialization.json.DecodeSequenceMode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeToSequence
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private var mState: PieChartState? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val chart = findViewById<PieChartView>(R.id.pieChart)
        mState = createState()
        chart.setValue(mState!!)
        chart.setOnSectorSelectListener {
            mState = mState?.copy(selected = it)
            chart.setValue(mState!!)
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
        PieChartState(it, it[3])
    }

}