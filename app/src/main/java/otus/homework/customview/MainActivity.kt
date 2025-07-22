package otus.homework.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import otus.homework.customview.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val payloads by lazy { this.readData() }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        initCategory()
    }

    private fun initPieChart() {
        val pieces = payloads.mapPieces()
        /* binding.pieChart.setPieces(pieces) */
    }

    private fun initCategory() {
        val charts = payloads.mapCategories()
        binding.view.setCharts(charts)
    }
}