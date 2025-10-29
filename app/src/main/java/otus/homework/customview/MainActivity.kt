package otus.homework.customview

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import otus.homework.customview.data.Transaction
import otus.homework.customview.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.pieChartView.setTransactions(parseTransactions())

        binding.pieChartView.setOnCategoryClickListener { category ->
            Toast.makeText(this, category.name, Toast.LENGTH_SHORT).show()
        }
    }

    private fun parseTransactions(): List<Transaction> {
        val json = resources.openRawResource(R.raw.payload)
            .bufferedReader().use { it.readText() }
        val type = object : TypeToken<List<Transaction>>() {}.type
        return Gson().fromJson(json, type)
    }
}