package otus.homework.customview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val raw = resources.openRawResource(R.raw.payload).bufferedReader().use { it.readText() }
        val array = JSONArray(raw)
        val data = mutableListOf<DataItem>()
        for (i in 0 until array.length()) {
            (array[i] as JSONObject).let {
                data.add(
                    DataItem(
                        it.getLong("id"),
                        it.getString("name"),
                        it.getInt("amount"),
                        it.getString("category"),
                        it.getLong("time")
                    )
                )
            }
        }
        val categories = data.groupBy { it.category }
            .mapValues { it.value.sumBy { it.amount }.toFloat() }
        findViewById<CategoriesPieChart>(R.id.pie_chart).let {
            it.setValues(categories)
            it.listener = CategoriesPieChart.CategoryClickListener { category ->
                findViewById<CategoryLineChart>(R.id.line_chart).setValues(data.filter { it.category == category })
            }
        }
    }
}