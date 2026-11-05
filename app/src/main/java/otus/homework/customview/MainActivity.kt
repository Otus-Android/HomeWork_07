package otus.homework.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var pieChartView: PieChartView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        pieChartView = findViewById(R.id.pieChartView)

        val repository = ExpenseRepository(this)

        val expenses = repository.loadExpenses()

        pieChartView.setExpenses(expenses)

        pieChartView.setOnCategoryClickListener { category ->
            Toast.makeText(
                this,
                category,
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}