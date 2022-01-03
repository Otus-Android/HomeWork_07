package otus.homework.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.annotation.RawRes
import com.google.gson.Gson
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val pieChart = findViewById<CustomPieChart>(R.id.pie_chart)

        val payloadData = getPayloadData()

        initPieChart(pieChart, payloadData)
    }

    private fun initPieChart(pieChart: CustomPieChart, payload: Array<PayloadData>?) {
        val pieData = PieData(resources.getStringArray(R.array.chart_colors))
        payload?.map { pieData.add(it.name, it.amount) }
        pieChart.setData(pieData)
        pieChart.setOnSliceClickListener(object : CustomPieChart.OnSliceClickListener {
            override fun onClick(category: String) {
                Log.d("ddd", "name $category")
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