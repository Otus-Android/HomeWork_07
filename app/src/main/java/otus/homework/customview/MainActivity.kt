package otus.homework.customview

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import otus.homework.customview.piechart.PieChartView
import java.io.IOException

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val chartView = findViewById<PieChartView>(R.id.pieChartView)

        val expensesList = readDataFromJson()
        chartView.setData(expensesList)
        chartView.setOnSliceClick {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
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
}