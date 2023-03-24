package otus.homework.customview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import otus.homework.customview.custom_views.PieChart
import otus.homework.customview.extensions.getPayloadsFromJson

class MainActivity : AppCompatActivity() {

    private val pieChartView by lazy(LazyThreadSafetyMode.NONE) {
        findViewById<PieChart>(R.id.pie_chart)
    }

    private val floatingButton by lazy(LazyThreadSafetyMode.NONE) {
        findViewById<FloatingActionButton>(R.id.floating_button)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initPieChart()

        floatingButton.setOnClickListener {
            startActivity(SecondActivity.getNewIntent(this))
        }
    }

    private fun initPieChart() {
        val payloads = getPayloadsFromJson(R.raw.payload)
        pieChartView.setPayloadsData(payloads)
    }
}