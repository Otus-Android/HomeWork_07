package otus.homework.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import org.json.JSONArray
import otus.homework.customview.charts.PayloadEntity
import otus.homework.customview.charts.pie.OnPieSliceClickListener
import otus.homework.customview.charts.pie.PieChartView
import otus.homework.customview.utils.getRawTextFile

class MainActivity : AppCompatActivity(), OnPieSliceClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rawData = resources.getRawTextFile(R.raw.payload)
        val jsonArray = JSONArray(rawData)

        val payload = ArrayList<PayloadEntity>()
        for (i in 0 until jsonArray.length()){
            val jsonEntry = jsonArray.getJSONObject(i)
            payload.add(
                PayloadEntity(
                    id = jsonEntry.getInt("id"),
                    name = jsonEntry.getString("name"),
                    amount = jsonEntry.getInt("amount"),
                    category = jsonEntry.getString("category"),
                    time = jsonEntry.getInt("time")
                )
            )
        }

        val chartView = findViewById<PieChartView>(R.id.piechart)
        chartView.updatePayload(payload)
        chartView.setOnPieSliceClickListener(this)

    }

    override fun onClick(entry: PayloadEntity) {
        //TODO("Not yet implemented")
    }

}