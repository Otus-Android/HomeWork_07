package otus.homework.customview

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import otus.homework.customview.custom_views.LineChart
import otus.homework.customview.extensions.getPayloadsFromJson

class SecondActivity : AppCompatActivity() {

    private val lineChartView by lazy(LazyThreadSafetyMode.NONE) {
        findViewById<LineChart>(R.id.line_chart)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        initLineChart()
    }

    private fun initLineChart() {
        val payloads = getPayloadsFromJson(R.raw.payload_advanced)
        lineChartView.setLineChartData(payloads)
    }

    companion object {
        fun getNewIntent(context: Context): Intent {
            return Intent(context, SecondActivity::class.java)
        }
    }

}