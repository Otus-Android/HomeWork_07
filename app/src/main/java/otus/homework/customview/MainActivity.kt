package otus.homework.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import org.json.JSONArray
import otus.homework.customview.charts.PayloadEntity
import otus.homework.customview.charts.pie.OnPieSliceClickListener
import otus.homework.customview.charts.pie.PieChartView
import otus.homework.customview.charts.pie.PiePayloadEntity
import otus.homework.customview.utils.getRawTextFile

class MainActivity : AppCompatActivity(), OnPieSliceClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rawData = resources.getRawTextFile(R.raw.payload)
        val jsonArray = JSONArray(rawData)

        val payload = ArrayList<PayloadEntity>()
        val categories = HashMap<String, Int>()
        for (i in 0 until jsonArray.length()){
            val jsonEntry = jsonArray.getJSONObject(i)

            val category = jsonEntry.getString("category")
            val amount = jsonEntry.getInt("amount")

            categories[category] = (categories[category] ?: 0) + amount

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

        val piePayload = categories.map { PiePayloadEntity(it.key, it.value) }

        val chartView = findViewById<PieChartView>(R.id.piechart)
        chartView.updatePayload(piePayload)
        chartView.setOnPieSliceClickListener(this)

    }

    override fun onClick(entry: PayloadEntity) {
        //TODO("Not yet implemented")
    }

}