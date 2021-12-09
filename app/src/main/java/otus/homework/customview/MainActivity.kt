package otus.homework.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
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
    private var pieChart: PieChart? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        models = ExpenseModel.readExpensesFromJson(this)

        val button = findViewById<Button>(R.id.button)
        pieChart = findViewById<PieChart>(R.id.pie_chart)

        button.setOnClickListener {
            models?.let {
                pieData = convertToPieData(it)
                pieChart?.setData(pieData)
                pieChart?.setOnCategoryClickListener { name ->
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

    }

    private fun convertToPieData(models: List<ExpenseModel>): PieData {
        val pieData = PieData(null)
        models.forEach { model ->
            pieData.add(
                model.category,
                model.amount
            )
        }
        return pieData
    }
}