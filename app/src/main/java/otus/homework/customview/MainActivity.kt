package otus.homework.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import org.json.JSONArray
import otus.homework.customview.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val expenses = readData()
        binding.pieChart.setExpenses(expenses)
        binding.chart.setExpenses(expenses)
    }
    private fun readData(): List<Expenses> {
        val jsonArray = JSONArray(this.resources.openRawResource(R.raw.payload).reader().readText())
        return (0 until jsonArray.length()).map {
            val jsonObj = jsonArray.getJSONObject(it)
            return@map Expenses(
                jsonObj.optInt("id"),
                jsonObj.optString("name", ""),
                jsonObj.optInt("amount"),
                jsonObj.optString("category", ""),
                jsonObj.optLong("time")
            )
        }
    }
}
