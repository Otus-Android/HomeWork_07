package otus.homework.customview

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import otus.homework.customview.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val testPayData = parseJson(this, R.raw.payload)
        for (item in testPayData) {
            Log.d("MainActivity", item.toString())
        }
        val categories = testPayData.groupBy { it.category }
        Log.d("MainActivity", "countCategories: $categories")
        for (it in categories) {
            val amount = it.value.sumOf { it.amount }
            Log.d("MainActivity", "category: ${it.key}" + " amount: $amount")
        }

        binding.pieChartView.setData(testPayData)
        binding.pieChartView.setOnCategoryClickListener { category ->
            Toast.makeText(this, category, Toast.LENGTH_SHORT).show()
            Log.d("MainActivity", "category1: $category")
        }
    }

    private fun parseJson(context: Context, resId: Int): List<PieChartData> {
        context.resources.openRawResource(resId).use { inputStream ->
            val json = inputStream.bufferedReader().readText()
            val type = object : TypeToken<List<PieChartData>>() {}.type
            return Gson().fromJson(json, type)
        }
    }
}