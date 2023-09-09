package otus.homework.customview

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import otus.homework.customview.DetailsActivity.Companion.KEY_LIST
import otus.homework.customview.databinding.ActivityMainBinding
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var data: List<PayloadItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            val inputStream = resources.openRawResource(R.raw.payload)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val json = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                json.append(line)
            }
            reader.close()

            val gson = Gson()
            val type = object : TypeToken<List<PayloadItem>>() {}.type
            data = gson.fromJson(json.toString(), type)

            binding.chart.setup(
                data
                    .groupBy { it.category }
                    .map { (cat, amounts) ->
                        PieChart.Category.OneCategory(cat, amounts.sumBy { it.amount }) })
        }

        binding.chart.onCategoryClickListener = object : PieChart.OnCategoryClickListener {
            override fun onClick(category: PieChart.Category) {
                val text = when (category) {
                    is PieChart.Category.MultipleCategories -> "${category.names} -> ${category.value}"
                    is PieChart.Category.OneCategory -> "${category.name} -> ${category.value}"
                }
                Toast.makeText(this@MainActivity, text, Toast.LENGTH_SHORT).show()

                val list = data.filter {
                    when (category) {
                        is PieChart.Category.MultipleCategories -> it.category in category.names
                        is PieChart.Category.OneCategory -> it.category == category.name
                    }
                }.map { DetailsChart.PayItem(it.amount, it.time) }



                val intent = Intent(this@MainActivity, DetailsActivity::class.java)
                intent.putExtra(KEY_LIST, Gson().toJson(list))
                startActivity(intent)
            }
        }


    }
}
