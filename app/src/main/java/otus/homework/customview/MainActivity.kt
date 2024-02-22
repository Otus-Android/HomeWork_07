package otus.homework.customview

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.serialization.json.Json
import otus.homework.customview.customView.LinearView
import otus.homework.customview.customView.PieChartView
import otus.homework.customview.models.Expense

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            val expenses = readJsonFromRaw(this, R.raw.payload)
            val pieChart = findViewById<PieChartView>(R.id.pieChart)
            val linearView = findViewById<LinearView>(R.id.linearView)
            val categories = expenses?.groupBy { it.category }
            categories?.let {
                pieChart.setData(categories)
                linearView.setData(categories)
                linearView.setColors(pieChart.colors)
            }
        }
    }

    private fun readJsonFromRaw(context: Context, rawResourceId: Int): List<Expense>? {
        val inputStream = context.resources.openRawResource(rawResourceId).bufferedReader().use {
            it.readText()
        }

        return try {
            Json.decodeFromString<List<Expense>>(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}