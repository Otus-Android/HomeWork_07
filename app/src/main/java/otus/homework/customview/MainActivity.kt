package otus.homework.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import otus.homework.customview.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.chart.setValues(listOf(1500, 499, 129, 4541, 1600, 1841, 369, 100, 8000, 809, 1000, 389))

    }
}