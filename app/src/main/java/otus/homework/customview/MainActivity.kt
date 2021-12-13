package otus.homework.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import otus.homework.customview.chartview.PieChart
import otus.homework.customview.chartview.PieData
import otus.homework.customview.linechartview.LineChart
import otus.homework.customview.linechartview.LineData
import otus.homework.customview.model.ExpenseModel

class MainActivity : AppCompatActivity() {

    private var models: List<ExpenseModel>? = null
    private var pieData: PieData? = null
    private var toast: Toast? = null
    private var pieChart: PieChart? = null
    private var lineChart: LineChart? = null
    private var lineData: LineData? = null
    private var pieChartVisibility: Int = View.GONE
    private var lineChartVisibility: Int = View.GONE
    private val pieVisibilityKey = "PieVisibilityKey"
    private val lineVisibilityKey = "LineVisibilityKey"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        models = ExpenseModel.readExpensesFromJson(this)

        savedInstanceState?.let {
            pieChartVisibility = it.getInt(pieVisibilityKey)
            lineChartVisibility = it.getInt(lineVisibilityKey)
        }

        showPieChart()
        showLineChart()

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(pieVisibilityKey, pieChartVisibility)
        outState.putInt(lineVisibilityKey, lineChartVisibility)
    }

    private fun showLineChart() {
        lineChart = findViewById<LineChart>(R.id.line_chart).apply {
            visibility = lineChartVisibility
        }
        val button = findViewById<Button>(R.id.button_line)
        models?.let {
            lineData = convertToLineData(it)
            lineChart?.setLineData(lineData)
        } ?: run {
            Log.e("MainActivity", "expense models are null")
        }
        button.setOnClickListener {
            pieChartVisibility = View.GONE
            pieChart?.visibility = pieChartVisibility

            lineChartVisibility = View.VISIBLE
            lineChart?.visibility = lineChartVisibility
        }
    }

    private fun convertToLineData(models: List<ExpenseModel>): LineData {
        val lineData = LineData(null)
        models.forEach { model ->
            lineData.add(
                model.category,
                model.amount,
                model.time
            )
        }
        return lineData
    }

    private fun showPieChart() {
        val button = findViewById<Button>(R.id.button_pie)
        pieChart = findViewById<PieChart>(R.id.pie_chart).apply {
            visibility = pieChartVisibility
        }
        pieChart?.setOnCategoryClickListener { name ->
            if (toast != null) {
                toast?.cancel()
            }
            toast = Toast.makeText(this, name, Toast.LENGTH_SHORT)
            toast?.show()
        }

        models?.let {
            pieData = convertToPieData(it)
            pieChart?.setData(pieData)
        } ?: run {
            Log.e("MainActivity", "expense models are null")
        }

        button.setOnClickListener {
            lineChartVisibility = View.GONE
            lineChart?.visibility = lineChartVisibility

            pieChartVisibility = View.VISIBLE
            pieChart?.visibility = pieChartVisibility
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