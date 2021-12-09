package otus.homework.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import otus.homework.customview.chartview.PieChart
import otus.homework.customview.chartview.PieData
import otus.homework.customview.model.ExpenseModel

class MainActivity : AppCompatActivity() {

    private var models: List<ExpenseModel>? = null
    private var pieData: PieData? = null
    private var toast: Toast? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        models = ExpenseModel.readExpensesFromJson(this)

        models?.let {
            pieData = convertToPieData(it)
            val pieChart = findViewById<PieChart>(R.id.pie_chart)
            pieChart.setData(pieData)
            pieChart.setOnCategoryClickListener { name ->
                if (toast != null) {
                    toast?.cancel()
                }
                toast = Toast.makeText(this, name, Toast.LENGTH_SHORT)
                toast?.show()
            }
        } ?: run {
            Log.e("MainActivity", "expense models are null")
        }
    }

    private fun convertToPieData(models: List<ExpenseModel>): PieData {
        val pieData = PieData()
        models.forEach { model ->
            pieData.add(
                model.category,
                model.amount
            )
        }
        return pieData
    }
}