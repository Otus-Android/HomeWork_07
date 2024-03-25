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

        val parsedData = parseData()
        binding.pieChart.setData(parsedData)
        binding.linearChart.setData(parsedData)
    }

    private fun parseData(): List<Category> {
        val rowData = JSONArray(
            this.resources
                .openRawResource(R.raw.payload)
                .reader()
                .readText()
        )
        return (0 until rowData.length()).map {
            val obj = rowData.getJSONObject(it)
            return@map Category(
                obj.optInt("id"),
                obj.optString("name", ""),
                obj.optInt("amount"),
                obj.optString("category", ""),
                obj.optLong("time")
            )
        }
    }
}