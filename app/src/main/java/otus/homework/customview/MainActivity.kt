package otus.homework.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : AppCompatActivity(),DrawListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val payloadArrayRaw = JSONArray(
            resources.openRawResource(R.raw.payload).bufferedReader().use { it.readText() }
        )
        val payloadList = convertRawData(payloadArrayRaw)
        val sortedList = payloadList.groupBy { it.category }
            .mapValues { value -> value.value.sumBy { it.amount }.toFloat() }
        findViewById<PieView>(R.id.flex2).setValues(sortedList)
       findViewById<Grapf>(R.id.flex).setValues(payloadList.toList())
        findViewById<PieView>(R.id.flex2).listener = PieView.ClickListener { chartElement ->
            Toast.makeText(this,chartElement,Toast.LENGTH_SHORT).show()
        }
        findViewById<PieView>(R.id.flex2).setlistener(this)
    }


    private fun convertRawData(data: JSONArray): MutableList<Spending> {
        val result = mutableListOf<Spending>()
        var index = 0
        while (index < data.length()) {
            (data[index] as JSONObject).apply {
                result.add(
                    Spending(
                        this.getLong("id"),
                        this.getString("name"),
                        this.getInt("amount"),
                        this.getString("category"),
                        this.getLong("time").toFloat()
                    )
                )
            }
            index++
        }
        return result
    }

    override fun drawDone() {
        findViewById<PieView>(R.id.flex2).startAnimation()
    }
}