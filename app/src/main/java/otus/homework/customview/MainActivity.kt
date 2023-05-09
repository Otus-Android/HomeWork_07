package otus.homework.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import otus.homework.customview.customview.LineChartView
import otus.homework.customview.customview.PieChartView
import otus.homework.customview.model.Expenses
import otus.homework.customview.model.ExpensesByCategory
import java.text.SimpleDateFormat

class MainActivity : AppCompatActivity() {

    private val expenses by lazy { ExpensesRepository(this).getExpenses() }
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<PieChartView>(R.id.pie).insertItems(getExpensesByCategory())
        findViewById<TextView>(R.id.total).text = getString(R.string.total, expenses.sumOf { it.amount }.toString())
        findViewById<PieChartView>(R.id.pie).touchListener = { exp ->
            findViewById<LineChartView>(R.id.lineChart).run {
                visibility = View.VISIBLE
                setData(expenses.getPointsLineChart(exp.category))
                postInvalidate()
            }
        }
    }

    private fun getExpensesByCategory(): MutableSet<ExpensesByCategory> {
        val categoryListWithSum = mutableSetOf<ExpensesByCategory>()
         expenses.map { it.category }.forEach {cat ->
            categoryListWithSum.add(ExpensesByCategory(cat,expenses.filter { it.category == cat }.sumOf { it.amount } ))
        }
        return categoryListWithSum
    }

    private fun List<Expenses>.getPointsLineChart(category: String): List<Float>{

        val k = filter { it.category == category }
        val g = k.map { it.amount.toFloat() / 100 }
        println()
        return g//listOf(8f,6f,1f,9f,5f,10f,3f,4f,7f,2f)
    }
}