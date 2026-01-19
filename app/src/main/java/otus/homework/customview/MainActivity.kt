package otus.homework.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val pieChartView = findViewById<PieChartView>(R.id.pie_chart_view)
        val lineChartView = findViewById<LineChartView>(R.id.lineChartView)

        val jsonString = this.resources.openRawResource(R.raw.payload).bufferedReader().use { it.readText() }
        val data = parsePayload(jsonString)
        pieChartView.setData(data)

        pieChartView.setOnSectorClickListener { category ->
            val categoryData = parseCategoryData(jsonString, category)
            Log.d("BarChartData", "Data for $category: $categoryData")
            lineChartView.setData(categoryData)

            lineChartView.visibility = View.VISIBLE
        }
    }

}
fun parsePayload(jsonString: String): List<Pair<String, Float>> {
    val jsonArray = JSONArray(jsonString)
    val categorySums = mutableMapOf<String, Float>()

    for (i in 0 until jsonArray.length()) {
        val obj = jsonArray.getJSONObject(i)
        val category = obj.getString("category")
        val amount = obj.getDouble("amount").toFloat()
        categorySums[category] = (categorySums[category] ?: 0f) + amount
    }

    // Преобразуем в список пар: категория - сумма
    return categorySums.map { it.key to it.value }
}

fun parseCategoryData(jsonString: String, category: String): List<Pair<String, Float>> {
    val jsonArray = JSONArray(jsonString)
    val categoryData = mutableListOf<Pair<String, Float>>()

    val dateFormatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    for (i in 0 until jsonArray.length()) {
        val item = jsonArray.getJSONObject(i)
        val itemCategory = item.getString("category")
        val amount = item.getDouble("amount").toFloat()
        val time = item.getLong("time") * 1000
        val date = dateFormatter.format(Date(time))

        // Если категория совпадает, добавляем в список
        if (itemCategory == category) {
            categoryData.add(date to amount)
        }
    }

    return categoryData
}