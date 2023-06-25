package otus.homework.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.Month
import org.threeten.bp.ZoneId
import otus.homework.customview.databinding.ActivityMainBinding

private const val TAG = "MainActivityTag"

class MainActivity : AppCompatActivity() {
    @OptIn(ExperimentalSerializationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val stream = resources.openRawResource(R.raw.payload)
        val charges = Json.decodeFromStream<List<Charge>>(stream)

        val startDate = LocalDate.of(2021, Month.JUNE, 8)
        val endDate = startDate.plusDays(7)
        val maxAmount = 12000

        binding.pieChart.setOnSelectListener { _, category ->
            binding.selectedCategory.text = category
            Toast.makeText(this, getString(R.string.category_selected, category), Toast.LENGTH_SHORT).show()
            binding.graph.setCharges(
                charges.filter { it.category == category },
                startDate,
                endDate,
                maxAmount
            )
        }

        binding.pieChart.setCharges(charges)
        binding.graph.setCharges(charges, startDate, endDate, maxAmount)
    }
}