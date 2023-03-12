package otus.homework.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private val expenses by lazy { ExpensesRepository(this).getExpenses() }
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val pieChart = findViewById<PieChart>(R.id.pie_chart)
        pieChart.items = expenses.fold(
            mutableMapOf<String, Int>()
        ) { acc, expenseItem ->
            acc[expenseItem.category] = (acc[expenseItem.category] ?: 0) + expenseItem.amount
            return@fold acc
        }.map {
            PieChart.Item(
                it.key,
                it.value
            )
        }
        val lineChart = findViewById<LineChart>(R.id.line_chart)
        pieChart.onItemTouch = {
            val expenses = expenses
                .filter { expense -> expense.category == it.label }
                .groupBy { it.time - it.time % DAY }
                .map {
                    ExpenseItem(
                        0,
                        "",
                        it.value.sumOf { it.amount },
                        it.value.first().category,
                        it.key
                    )
                }

            var serie = expenses.map { it.amount.toFloat()  }
            if (serie.size == 1) {
                serie = listOf(serie[0], serie[0])
            }
            lineChart.series = listOf(serie)
            lineChart.labels = expenses.map { dateFormat.format(Date(it.time * 1000)) }
            lineChart.requestLayout()
            lineChart.invalidate()
        }
    }

    companion object {
        const val DAY = 24 * 60 * 60
    }
}