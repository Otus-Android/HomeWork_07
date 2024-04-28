package otus.homework.customview

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import otus.homework.customview.CategoryView.Spending
import otus.homework.customview.PieChartView.Category
import java.io.BufferedReader
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.collections.Map.Entry

class MainActivity : AppCompatActivity() {

    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val consumptions = parseJson()
        if (consumptions != null) {
            setupPieChart(consumptions)
        }
    }

    private fun parseJson(): List<Consumption>? {
        val json = resources.openRawResource(R.raw.payload).bufferedReader().use(BufferedReader::readText)
        val gson = GsonBuilder().create()
        val type = object : TypeToken<ArrayList<Consumption>?>() {}.type
        return gson.fromJson(json, type)
    }

    private fun setupPieChart(data: List<Consumption>) {
        val categories = data.groupBy { it.category }.map { entry ->
            val categoryAmount = entry.value.sumOf { it.amount }
            Category(
                name = entry.key,
                amount = categoryAmount
            )
        }
        val pie = findViewById<PieChartView>(R.id.pie)
        pie.setup(categories)
        pie.setOnCategoryClickListener { setupCategoryView(data, it) }
    }

    private fun setupCategoryView(data: List<Consumption>, categoryName: String) {
        val categoryTitle = findViewById<TextView>(R.id.categoryTitle)
        val categoryView = findViewById<CategoryView>(R.id.category)
        categoryTitle.text = categoryName
        categoryView.setup(
            data.filter { it.category == categoryName }
                .groupBy { convertDate(it.time) }
                .map { entry: Entry<String, List<Consumption>> ->
                    val dayAmount = entry.value.sumOf { it.amount }
                    Spending(date = entry.key, amount = dayAmount)
                }
        )
    }

    private fun convertDate(time: Long): String {
        calendar.time = Date(secToMillis(time))
        return SimpleDateFormat("dd MMM", Locale.getDefault()).format(calendar.time)
    }

    private fun secToMillis(value: Long): Long = value * 1000
}