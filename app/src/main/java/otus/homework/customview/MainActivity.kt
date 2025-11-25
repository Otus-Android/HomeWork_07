package otus.homework.customview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val array = JSONArray(getJson())
        val payloads = ArrayList<PayloadEntity>()
        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)
            payloads.add(
                PayloadEntity(
                    item.getInt("id"),
                    item.getString("name"),
                    item.getInt("amount"),
                    item.getString("category"),
                    item.getLong("time"),
                )
            )
        }
        findViewById<PieChartView>(R.id.customView1).setPayloads(payloads)
        findViewById<GraphicsView>(R.id.customView2).setPayloads(payloads)
    }

    private fun getJson(): String {
        return resources.openRawResource(R.raw.payload).bufferedReader().use { it.readText() }
    }
}