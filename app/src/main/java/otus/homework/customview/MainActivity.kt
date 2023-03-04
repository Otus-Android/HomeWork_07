package otus.homework.customview

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import otus.homework.customview.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val expenses = decodeFromJsonFileResId<Expenses>(R.raw.payload)

        with(binding.pieChart) {
            setExpenses(expenses)
            setOnClickCategory { category ->
                val intent = LineChartActivity.getStartIntent(this@MainActivity, category)
                startActivity(intent)
            }
        }
    }
}