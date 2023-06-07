package otus.homework.customview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import otus.homework.customview.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    val chartModel : ChartModel

    init{
        chartModel = ChartModel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }
}