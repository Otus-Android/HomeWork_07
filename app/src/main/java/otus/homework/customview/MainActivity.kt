package otus.homework.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import otus.homework.customview.customview.PieChartView
import java.text.SimpleDateFormat

class MainActivity : AppCompatActivity() {

    private val expenses by lazy { ExpensesRepository(this).getExpenses() }
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<PieChartView>(R.id.pie).insertItems(expenses)
        findViewById<PieChartView>(R.id.pie).touchListener = {
            Toast.makeText(this, "${it.name} ${it.amount}", Toast.LENGTH_LONG).show()
        }
    }
}