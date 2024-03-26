package otus.homework.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import otus.homework.customview.custom.lineChart.LineChartView
import otus.homework.customview.custom.pieChart.ClickListenerPieChart
import otus.homework.customview.custom.ChartData
import otus.homework.customview.custom.pieChart.PieChartView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val pieChartView: PieChartView = findViewById(R.id.pieChartView)
        val lineChartView: LineChartView = findViewById(R.id.lineChartView)

        val gson = Gson()
        val inputStream = this.resources.openRawResource(R.raw.payload)
        val jsonString = inputStream.bufferedReader().use { it.readText() }
        val itemType = object : TypeToken<List<ChartData>>() {}.type
        val itemList: MutableList<ChartData> = gson.fromJson(jsonString, itemType)

        pieChartView.setData(itemList)
        lineChartView.setData(itemList)

        pieChartView.setClickListener(object : ClickListenerPieChart {
            override fun click(category: String) {
                val listCategory = itemList.filter { it.category == category }
                lineChartView.setData(listCategory)
            }
        })
    }
}