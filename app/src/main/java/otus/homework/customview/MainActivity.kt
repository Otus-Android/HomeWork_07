package otus.homework.customview

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<ExpenditurePieChartView>(R.id.expenditure_pie_chart).apply {
            val expenditureChartSegments = getChartSegments()
            setupData(expenditureChartSegments) { category: String ->
                Toast.makeText(this@MainActivity,"category: $category", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getChartSegments(): MutableList<ChartSegment> {
        val expenditures = readExpenditures()
        val totalAmount = expenditures.fold(0f) { acc, expenditure -> acc.plus(expenditure.amount) }
        val maxAmount = expenditures.maxOf { it.amount }
        var previousAngle = 0f
        return expenditures.map { expenditure ->
            val angleValue = (expenditure.amount * 360f).roundToInt() / totalAmount
            val color = if (TESTING_MODE) getTestColor(expenditure.id) else ColorGenerator.generateColor()
            ChartSegment(
                id = expenditure.id,
                name = expenditure.name,
                amount = expenditure.amount,
                category = expenditure.category,
                startAngle = previousAngle,
                endAngle = previousAngle + angleValue,
                percentageOfMaximum = (expenditure.amount * 100f).roundToInt() / maxAmount,
                color = color
            ).also { previousAngle += angleValue }
        }.toMutableList()
    }

    @ColorInt
    private fun getTestColor(id: Int): Int {
        return when(id) {
            1 -> Color.rgb(131,88,246)
            2 -> Color.rgb(240,143,103)
            3 -> Color.rgb(93,111,246)
            4 -> Color.rgb(112,224,184)
            5 -> Color.rgb(106,181,224)
            else -> ColorGenerator.generateColor()
        }
    }

    private fun readExpenditures(): List<Expenditure> {
        val payloadResource = if (TESTING_MODE) R.raw.test_payment else R.raw.payload
        val text = resources
            .openRawResource(payloadResource)
            .bufferedReader()
            .use { it.readText() }

        val gson = Gson()
        val type = object : TypeToken<List<Expenditure>>() {}.type
        return gson.fromJson(text, type)
    }

    companion object {
        private const val TESTING_MODE = false
    }
}