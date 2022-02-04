package otus.homework.customview

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.TypedValue
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONException


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val pieChartView = findViewById<PieChartView>(R.id.pie_chart)

        val jsonString = resources.openRawResource(R.raw.payload).bufferedReader().use {
            it.readText()
        }
        val jsonArray = try {
            JSONArray(jsonString)
        } catch (e: JSONException) {
            Toast.makeText(this, "Couldn't read resources json", Toast.LENGTH_SHORT).show()
            null
        }
        val chartData = jsonArray?.let {
            val list = mutableListOf<ChartData>()
            try {
                for (i in 0 until it.length()) {
                    val obj = it.getJSONObject(i)
                    list.add(
                        ChartData(
                            amount = obj.getInt("amount"),
                            name = obj.getString("name"),
                            id = obj.getInt("id"),
                            category = obj.getString("category")
                        )
                    )
                }
            } catch (e: JSONException) {
                Toast.makeText(this, "Error when parsing", Toast.LENGTH_SHORT).show()
            }
            list
        }
        chartData?.let {
            pieChartView.setData(it)
        }
    }
}
