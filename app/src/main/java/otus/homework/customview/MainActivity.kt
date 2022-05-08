package otus.homework.customview

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val result = resources.openRawResource(R.raw.payload).bufferedReader().use { it.readText() }
        val type = TypeToken.getParameterized(
            ArrayList::class.java,
            Item::class.java).type
        val items = Gson().fromJson<ArrayList<Item>>(result, type)

        val pieChartView = findViewById<PieChartView>(R.id.pieChartView)
        pieChartView.setItems(items)
        pieChartView.categoryClickedListener = object : PieChartView.Listener {
            override fun clickCategory(category: Category) {
                Log.d("MainActivity", "category ${category.toString()}")
            }
        }

        val graphView = findViewById<GraphView>(R.id.graphView)
        graphView.setItems(items)
    }
}