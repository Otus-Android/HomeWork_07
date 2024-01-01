package otus.homework.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    private val expenses by lazy { ExpensesRepository(this).getExpenses() }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<PieChartView>(R.id.pie).setData(getExpenses())

        findViewById<PieChartView>(R.id.pie).touchListener = { expense ->

            Toast.makeText(this, "${expense.category}: ${expense.amount}", Toast.LENGTH_SHORT)
                .show()

            findViewById<LineChartView>(R.id.lineChart).run {
                setData(expenses.filter { it.categoryName == expense.category }
                    .map { it.amount.toFloat() / 100 })
                postInvalidate()
            }
        }
    }

    private fun getExpenses(): MutableSet<Category> {

        val categories = mutableSetOf<Category>()
        expenses.map { it.categoryName }.forEach { cat ->
            categories.add(
                Category(
                    cat,
                    expenses.filter { it.categoryName == cat }.sumOf { it.amount })
            )
        }
        return categories
    }
}
