package otus.homework.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream

class MainActivity : AppCompatActivity() {
    private lateinit var pieChart: PieChart
    @OptIn(ExperimentalSerializationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pieChart = findViewById(R.id.pie_cart)
        pieChart.setOnSelectListener { _, category ->
            Toast.makeText(this, getString(R.string.category_selected, category), Toast.LENGTH_SHORT).show()
        }

        val stream = resources.openRawResource(R.raw.payload)
        val charges = Json.decodeFromStream<List<Charge>>(stream)

        println(charges)

        pieChart.setCharges(charges)
    }
}