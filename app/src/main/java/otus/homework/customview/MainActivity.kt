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

        val stream = resources.openRawResource(R.raw.payload)
        val charges = Json.decodeFromStream<List<Charge>>(stream)

        setContentView(binding.root)
        binding.pieChart.setOnSelectListener { _, category ->
            binding.selectedCategory.text = category
            Toast.makeText(this, getString(R.string.category_selected, category), Toast.LENGTH_SHORT).show()
            binding.graph.setCharges(
                charges.filter { it.category == category },
                LocalDate.of(2021, Month.JUNE, 1),
                LocalDate.of(2021, Month.JUNE, 14)
            )
        }

//        val endDate = charges.maxOfOrNull { it.time }?.secondsToLocalDate()
//        val startDate = charges.minOfOrNull { it.time }?.secondsToLocalDate()
//
//        Log.d(TAG, "startDate: $startDate, endDate: $endDate")

        binding.pieChart.setCharges(charges)
        binding.graph.setCharges(
            charges,
            LocalDate.of(2021, Month.JUNE, 1),
            LocalDate.of(2021, Month.JUNE, 14)
        )
    }
}