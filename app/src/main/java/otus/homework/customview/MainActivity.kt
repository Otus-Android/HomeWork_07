package otus.homework.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import otus.homework.customview.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    @OptIn(ExperimentalSerializationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        binding.pieChart.setOnSelectListener { _, category ->
            binding.selectedCategory.text = category
            Toast.makeText(this, getString(R.string.category_selected, category), Toast.LENGTH_SHORT).show()
        }

        val stream = resources.openRawResource(R.raw.payload)
        val charges = Json.decodeFromStream<List<Charge>>(stream)

        println(charges)

        binding.pieChart.setCharges(charges)
    }
}