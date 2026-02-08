package otus.homework.customview

import android.os.Bundle
import android.text.format.DateFormat
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.Gson
import java.util.Date

class MainActivity : AppCompatActivity() {

    private lateinit var detailsTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        detailsTextView = findViewById(R.id.detailsTextView)

        val jsonString = loadJsonFromRaw()
        val transactions = parseTransactions(jsonString)
        val categoriesData = groupTransactionsByCategory(transactions)

        val pieChartView = findViewById<PieChartView>(R.id.pieChartView)
        pieChartView.setData(categoriesData)
        pieChartView.setOnSectorClickListener(object : PieChartView.OnSectorClickListener {
            override fun onSectorClick(categoryData: CategoryData) {
                showCategoryDetails(categoryData)
            }
        })
    }

    private fun showCategoryDetails(categoryData: CategoryData) {
        val stringBuilder = StringBuilder()

        stringBuilder.append("Категория: ${categoryData.category}\n")
        stringBuilder.append("Общая сумма: ${String.format("%.2f", categoryData.amount)} руб.\n")
        stringBuilder.append("Количество транзакций: ${categoryData.transactions.size}\n\n")

        stringBuilder.append("Детали транзакций:\n")
        stringBuilder.append("-------------------\n")

        categoryData.transactions.forEach { transaction ->
            stringBuilder.append("Название: ${transaction.name}\n")
            stringBuilder.append("Сумма: ${String.format("%.2f", transaction.amount)} руб.\n")
            stringBuilder.append("Время: ${formatTime(transaction.time)}\n")
            stringBuilder.append("---\n")
        }

        detailsTextView.text = stringBuilder.toString()
    }

    private fun formatTime(timestamp: Long): String {
        val date = Date(timestamp * 1000L)
        return DateFormat.format("dd.MM.yyyy HH:mm", date).toString()
    }

    private fun loadJsonFromRaw(): String {
        return try {
            resources.openRawResource(R.raw.payload).bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            e.printStackTrace()
            "[]"
        }
    }

    private fun parseTransactions(jsonString: String): List<Transaction> {
        return try {
            Gson().fromJson(jsonString, Array<Transaction>::class.java).toList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun groupTransactionsByCategory(transactions: List<Transaction>): List<CategoryData> {
        val categoriesMap = mutableMapOf<String, MutableList<Transaction>>()

        transactions.forEach { transaction ->
            if (!categoriesMap.containsKey(transaction.category)) {
                categoriesMap[transaction.category] = mutableListOf()
            }
            categoriesMap[transaction.category]?.add(transaction)
        }

        val colors = arrayOf(
            ContextCompat.getColor(this, android.R.color.holo_red_dark),
            ContextCompat.getColor(this, android.R.color.holo_blue_dark),
            ContextCompat.getColor(this, android.R.color.holo_green_dark),
            ContextCompat.getColor(this, android.R.color.holo_orange_dark),
            ContextCompat.getColor(this, android.R.color.holo_purple),
            ContextCompat.getColor(this, android.R.color.darker_gray),
            ContextCompat.getColor(this, android.R.color.holo_blue_light),
            ContextCompat.getColor(this, android.R.color.holo_green_light),
            ContextCompat.getColor(this, android.R.color.holo_orange_light),
            ContextCompat.getColor(this, android.R.color.holo_red_light),
            ContextCompat.getColor(this, R.color.colorPrimary),
            ContextCompat.getColor(this, R.color.colorAccent)
        )

        return categoriesMap.entries.mapIndexed { index, (category, transList) ->
            val totalAmount = transList.sumOf { it.amount }
            CategoryData(
                category = category,
                amount = totalAmount,
                color = colors[index % colors.size],
                transactions = transList
            )
        }.sortedByDescending { it.amount }
    }
}