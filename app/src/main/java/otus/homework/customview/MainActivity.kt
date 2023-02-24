package otus.homework.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import otus.homework.customview.chart.pie.PieChart

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<PieChart>(R.id.pie_chart).setOnClickListener {
            Log.d(this::class.java.name, "$it")
        }
    }
}