package otus.homework.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import org.json.JSONArray

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        findViewById<PieChart>(R.id.pie).apply {
//            setData(getExpenses())
//            setOnClickListenerItem {
//                Toast.makeText(applicationContext, "${it.name}:${it.amount}", Toast.LENGTH_SHORT)
//                    .show()
//            }
//        }

        findViewById<GraphChart>(R.id.pie).setData(listOf(10f, 20f, 40f, 30f, 20f, 10f, 20f, 40f, 30f, 20f))
    }

    private fun getExpenses(): List<Playload> {
        val jsonArray = JSONArray(
            applicationContext.resources.openRawResource(R.raw.payload).reader().readText()
        )
        return (0 until jsonArray.length()).map {
            val jsonObj = jsonArray.getJSONObject(it)
            Playload(
                jsonObj.optInt("id"),
                jsonObj.optString("name", ""),
                jsonObj.optInt("amount"),
            )
        }
    }
}
