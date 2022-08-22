package otus.homework.customview

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.gson.Gson
import java.security.SecureRandom
import kotlin.math.max
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val categories = loadCategories()

        val pieChart = findViewById<PieChartView>(R.id.pie_chart)
        val lineChart = findViewById<LineChartView>(R.id.line_chart)

        var toast: Toast? = null

        pieChart.onClicked = { name ->
            val category = categories.first { it.title == name }

            toast?.cancel()
            toast = Toast.makeText(this,
                "${category.title} (${category.totalAmount})",
                Toast.LENGTH_SHORT).also {
                it.show()
            }

            lineChart.setCategory(category)
        }

        if (savedInstanceState == null) {
            pieChart.setData(categories)
        }
    }

    private fun loadCategories(): List<ExpenseCategory> {
        val expenses = resources.openRawResource(R.raw.payload).bufferedReader().use {
            Gson().fromJson(it, Array<Expense>::class.java)
        }

        val random = RandomColors()

        return expenses
            .groupBy { it.category }
            .map { category ->
                ExpenseCategory(
                    title = category.key,
                    totalAmount = category.value.sumBy { it.amount },
                    dates = category.value.map { it.time },
                    amounts = category.value.map { it.amount },
                    color = random.getColor()
                )
            }
            .sortedByDescending { it.totalAmount }
    }
}