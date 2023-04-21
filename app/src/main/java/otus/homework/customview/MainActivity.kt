package otus.homework.customview

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import otus.homework.customview.view.linechart.LineChartView
import otus.homework.customview.view.piechart.PieChartView
import java.io.IOException

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /**
         * Чтобы читать данные из файла payload.json нужно поставить isRead = true,
         * чтобы сгенерировать случайные записи, поставить isRead = false
         */
        val isRead = true

        val expensesList = if (isRead) {
            readDataFromJson()
        } else {
            RandomDataGenerator.generateRandomData()
        }

        INSTANCE = this

        setContentView(R.layout.activity_main)
        val pieChartView = findViewById<PieChartView>(R.id.pieChartView)
        val lineChartView = findViewById<LineChartView>(R.id.lineChartView)

        pieChartView.setData(expensesList)
        pieChartView.setOnSliceClick { category ->
            Toast.makeText(this, "category: $category", Toast.LENGTH_SHORT).show()
            lineChartView.setItems(expensesList.filter { it.category == category })
        }
    }

    private fun readDataFromJson(): List<Expense> {
        lateinit var jsonString: String
        try {
            jsonString = resources.assets.open("payload.json")
                .bufferedReader()
                .use { it.readText() }
        } catch (e: IOException) {
            Log.d(this::class.java.name, e.message ?: "reading json error")
        }

        val expensesType = object : TypeToken<List<Expense>>() {}.type

        return Gson().fromJson(jsonString, expensesType)
    }

    companion object {
        lateinit var INSTANCE: MainActivity
    }
}