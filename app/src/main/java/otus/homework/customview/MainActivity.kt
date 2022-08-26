package otus.homework.customview

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val pieChartButton = findViewById<MaterialButton>(R.id.pieChartButton)
        val statisticsButton = findViewById<MaterialButton>(R.id.statisticsButton)

        pieChartButton.setOnClickListener {
            startActivity(Intent(this, PieChartActivity::class.java))
        }

        statisticsButton.setOnClickListener {
            startActivity(Intent(this, StatisticsViewActivity::class.java))
        }
    }
}