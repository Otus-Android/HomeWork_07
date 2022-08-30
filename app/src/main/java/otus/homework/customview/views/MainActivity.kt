package otus.homework.customview.views

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.threeten.bp.*
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeFormatterBuilder
import org.threeten.bp.format.ResolverStyle
import org.threeten.bp.format.SignStyle
import org.threeten.bp.temporal.ChronoField
import otus.homework.customview.*
import otus.homework.customview.models.*
import otus.homework.customview.utils.ColorGenerator
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private val LOCAL_DATE_PATTERN: DateTimeFormatter by lazy {
        DateTimeFormatterBuilder()
            .appendValue(ChronoField.DAY_OF_MONTH, 1, 2, SignStyle.NORMAL)
            .toFormatter()
            .withResolverStyle(ResolverStyle.STRICT)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<ExpenditurePieChart>(R.id.expenditure_pie_chart).apply {
            val expenditures = readExpenditures(Chart.PIE)
            val pieChartSegments = getPieChartSegments(expenditures)
            setupData(pieChartSegments) { category: String ->
                Toast.makeText(this@MainActivity, "category: $category", Toast.LENGTH_SHORT).show()
            }
        }
        findViewById<ExpenditureLinearChart>(R.id.expenditure_linear_chart).apply {
            val expenditures = readExpenditures(Chart.LINEAR)
            val categories = getCategoriesMap(expenditures)
            setupData(categories, 20000)
        }
    }

    private fun readExpenditures(
        chartType: Chart
    ): List<Expenditure> {
        val payloadResource = if (TESTING_MODE) when (chartType) {
            Chart.PIE -> R.raw.test_pie_chart_payload
            Chart.LINEAR -> R.raw.test_linear_chart_payload
        } else R.raw.payload
        val text = resources
            .openRawResource(payloadResource)
            .bufferedReader()
            .use { it.readText() }

        val gson = Gson()
        val type = object : TypeToken<List<Expenditure>>() {}.type
        return gson.fromJson(text, type)
    }


    /*
    * Methods for linear chart points getting
    * */

    private fun getCategoriesMap(expenditures: List<Expenditure>): HashMap<ExpenditureCategory, ArrayList<LinearChartPoint>> {
        val resultMap = initDefaultMap()
        expenditures.forEach { expenditure ->
            val chartPoint = mapLinearChartPoint(expenditure)
            chartPoint.mapExpenditureCategory().let { key ->
                resultMap[key]?.add(chartPoint)
            }
        }
        return resultMap
    }

    private fun initDefaultMap(): HashMap<ExpenditureCategory, ArrayList<LinearChartPoint>> {
        return hashMapOf<ExpenditureCategory, ArrayList<LinearChartPoint>>().apply {
            put(ExpenditureCategory.PRODUCTS, arrayListOf())
            put(ExpenditureCategory.HEALTH, arrayListOf())
            put(ExpenditureCategory.EATING_OUT, arrayListOf())
            put(ExpenditureCategory.ALCOHOL, arrayListOf())
            put(ExpenditureCategory.DELIVERY, arrayListOf())
            put(ExpenditureCategory.TRANSPORT, arrayListOf())
            put(ExpenditureCategory.SPORT, arrayListOf())
        }
    }

    private fun mapLinearChartPoint(expenditure: Expenditure) =
        LinearChartPoint(
            id = expenditure.id,
            name = expenditure.name,
            amount = expenditure.amount,
            category = expenditure.category,
            time = expenditure.time,
            dayInMonth = mapExpenditureTime(expenditure.time)
        )

    private fun mapExpenditureTime(timestamp: Long): Int {
        val instant = Instant.ofEpochSecond(timestamp)
        val localDate = LocalDateTime.ofInstant(instant, ZoneOffset.UTC)
        return LOCAL_DATE_PATTERN.format(localDate).toInt()
    }


    /*
    * Methods for pie chart segments getting
    * */

    private fun getPieChartSegments(expenditures: List<Expenditure>): List<PieChartSegment> {
        val totalAmount = expenditures.fold(0f) { acc, expenditure -> acc.plus(expenditure.amount) }
        val maxAmount = expenditures.maxOf { it.amount }
        var previousAngle = 0f
        return expenditures.map { expenditure ->
            val angleValue = (expenditure.amount * 360f).roundToInt() / totalAmount
            val color =
                if (TESTING_MODE) getTestColor(expenditure.id) else ColorGenerator.generateColor()
            PieChartSegment(
                id = expenditure.id,
                name = expenditure.name,
                amount = expenditure.amount,
                category = expenditure.category,
                startAngle = previousAngle,
                endAngle = previousAngle + angleValue,
                percentageOfMaximum = (expenditure.amount * 100f).roundToInt() / maxAmount,
                color = color
            ).also { previousAngle += angleValue }
        }
    }

    @ColorInt
    private fun getTestColor(id: Int): Int {
        return when (id) {
            1 -> Color.rgb(131, 88, 246)
            2 -> Color.rgb(240, 143, 103)
            3 -> Color.rgb(93, 111, 246)
            4 -> Color.rgb(112, 224, 184)
            5 -> Color.rgb(106, 181, 224)
            else -> ColorGenerator.generateColor()
        }
    }


    companion object {
        private const val TESTING_MODE = true
    }
}