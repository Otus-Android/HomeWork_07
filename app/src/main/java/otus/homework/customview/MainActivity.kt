package otus.homework.customview

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import otus.homework.customview.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    val chartModel: ChartModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        binding.chartView.chartModel = chartModel
        val touchDown = binding.chartView._clickSector
        touchDown.observe(this, {
            chartModel.setChecked(it)
            chartModel.setScale(it)
            binding.chartView.invalidate()
        })
        setContentView(view)
    }
}