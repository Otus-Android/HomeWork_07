package otus.homework.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import otus.homework.customview.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // get dto from json
        val stringJson = resources.openRawResource(R.raw.payload)
            .bufferedReader()
            .use { it.readText() }
        val dto = Gson().fromJson(stringJson, Array<PurchaseDto>::class.java)

        // create dataset for the pie-chart from dto
        val pieChartData = mutableMapOf<String, Int>()
        dto.forEach {
            if (pieChartData.containsKey(it.category)) {
                val currentAmount = pieChartData[it.category]!!
                pieChartData[it.category] = currentAmount + it.amount
            } else {
                pieChartData[it.category] = it.amount
            }
        }
        // create dataset for the line-chart from the dto
        // by filtering initial dataset by needed category,
        // e.g. "Продукты"
        val lineChartCategoryName = "Продукты"
        val lineChartData = mutableMapOf<String, Int>()
        dto.sortedBy { it.time }
            .filter { it.category == lineChartCategoryName }
            .forEach {
                val stringDate = Date(it.time).toStringView()
                if (lineChartData.containsKey(stringDate)) {
                    val currentAmount = lineChartData[stringDate]!!
                    lineChartData[stringDate] = currentAmount + it.amount
                } else {
                    lineChartData[stringDate] = it.amount
                }
            }

        // The custom view state is defining by dataset.
        // We set data when activity first start only.
        // The custom view will save it's state on device config change.
        if (savedInstanceState == null) {
            //binding.pieChartView.updateData(data)
            /*binding.lineChartView.setData(
                categoryName = lineChartCategoryName,
                spendingByTimeData = lineChartData
            )*/
        }
        binding.lineChartView.setData(
            categoryName = lineChartCategoryName,
            spendingByTimeData = lineChartData
        )

        /*binding.pieChartView.setOnCategoryClickListener {
            Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
        }*/



    }
}


fun String.log() {
    Log.d("myLog", this)
}

@Throws(IllegalArgumentException::class)
fun Date.toStringView(): String {
    val stringPattern = "d MMM"
    val simpleDateFormat = SimpleDateFormat(stringPattern, Locale.getDefault())
    return simpleDateFormat.format(this)
}