package otus.homework.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import org.json.JSONArray
import org.json.JSONObject
import otus.homework.customview.views.CustomLineChart
import otus.homework.customview.views.CustomPieChart

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val pieView = findViewById<CustomPieChart>(R.id.pie_chart)
        val lineView = findViewById<CustomLineChart>(R.id.line_chart)

        val payloadArrayRaw = JSONArray(
            resources.openRawResource(R.raw.payload).bufferedReader().use { it.readText() }
        )
        val payloadList = convertRawData(payloadArrayRaw)
        val sortedList = payloadList.groupBy { it.category }
            .mapValues { value -> value.value.sumBy { it.amount }.toFloat() }

        pieView.setValues(sortedList)

        pieView.listener = CustomPieChart.ClickListener { pieElement ->
            lineView.setValues(payloadList.filter { it.category == pieElement })
        }
    }

    private fun convertRawData(data: JSONArray): MutableList<ExpensesItem> {
        val result = mutableListOf<ExpensesItem>()
        var index = 0
        while (index < data.length()) {
            (data[index] as JSONObject).apply {
                result.add(
                    ExpensesItem(
                        this.getLong("id"),
                        this.getString("name"),
                        this.getInt("amount"),
                        this.getString("category"),
                        this.getLong("time")
                    )
                )
            }
            index++
        }
        return result
    }
}