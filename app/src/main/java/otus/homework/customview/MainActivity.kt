package otus.homework.customview

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import otus.homework.customview.pie.PieCartCallback
import otus.homework.customview.pie.PieChart
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    init {
        Timber.plant(Timber.DebugTree())
    }

    var pieChart: PieChart? = null

    private val pieCharCallback = object : PieCartCallback {
        override fun sectorClick(category: String) {
            findViewById<TextView>(R.id.pieChartText).text = category
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val repository = SpendRepository(this)

        pieChart = findViewById<PieChart>(R.id.pieChart).apply {
            setCallback(pieCharCallback)
        }

        if (savedInstanceState == null) {
            pieChart?.setItems(repository.getSpendItems())
            pieChart?.startAnimationRotation()

        }
    }

    override fun onDestroy() {
        pieChart?.removeCallback()

        super.onDestroy()
    }
}
