package otus.homework.customview

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val pieChart = findViewById<ViraPieChartView>(R.id.pieChart)

        pieChart.setOnSectorClickListener { category ->
            Toast.makeText(this, category, Toast.LENGTH_SHORT).show()
        }

        val entries = loadEntriesFromRaw(R.raw.payload_7)
        pieChart.setData(entries)
    }

    private fun loadEntriesFromRaw(rawResId: Int): List<ViraPieChartView.Entry> {
        val json = resources.openRawResource(rawResId).bufferedReader().use { it.readText() }
        val array = JSONArray(json)
        val result = ArrayList<ViraPieChartView.Entry>(array.length())

        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val amount = obj.optLong("amount", 0L)
            if (amount <= 0L) continue

            result.add(
                ViraPieChartView.Entry(
                    name = obj.optString("name", ""),
                    category = obj.optString("category", ""),
                    time = obj.optLong("time", 0L),
                    amount = amount,
                )
            )
        }
        return result
    }
}