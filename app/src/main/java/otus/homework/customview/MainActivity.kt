package otus.homework.customview

import android.os.Bundle
import android.view.View
import androidx.annotation.RawRes
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import otus.homework.customview.line_chart.CustomLineChart
import otus.homework.customview.line_chart.LineData
import otus.homework.customview.pie_chart.CustomPieChart
import otus.homework.customview.pie_chart.PieData


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val pieChart = findViewById<CustomPieChart>(R.id.pie_chart)
        val lineChart = findViewById<CustomLineChart>(R.id.line_chart)
        val title = findViewById<View>(R.id.line_chart_title)

        val payloadData = getPayloadData()

        val pieData = PieData(resources.getStringArray(R.array.chart_colors))
        payloadData?.map { pieData.add(it.category, it.amount) }
        pieChart.setData(pieData)
        pieChart.setOnSliceClickListener(object : CustomPieChart.OnSliceClickListener {
            override fun onClick(category: String) {
                val lineData = LineData(category)
                payloadData?.filter { it.category == category }?.map { lineData.add(it.name, it.amount, it.time) }
                lineChart.setData(lineData)
                title.visibility = View.VISIBLE
            }
        })
    }

    private fun getPayloadData(): Array<PayloadData>? {
        val gson = Gson()
        return gson.fromJson(readRaw(R.raw.payload), Array<PayloadData>::class.java)
    }

    private fun readRaw(@RawRes resourceId: Int): String {
        return resources.openRawResource(resourceId).bufferedReader(Charsets.UTF_8).use { it.readText() }
    }
}