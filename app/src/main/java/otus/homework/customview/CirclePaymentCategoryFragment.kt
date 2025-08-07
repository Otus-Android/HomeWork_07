package otus.homework.customview

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import kotlinx.serialization.json.Json
import otus.homework.customview.model.TransactionJson
import otus.homework.customview.view.PieChartView
import otus.homework.customview.view.PieChartView.PieEntry

class CirclePaymentCategoryFragment : Fragment(R.layout.fragment_circle_payment_category) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val pieChartView = view.findViewById<PieChartView>(R.id.pie_chart)

        val jsonString =
            this.resources.openRawResource(R.raw.payload).bufferedReader().use { it.readText() }
        val data = parsePayload(jsonString)

        pieChartView.setEntriesItems(data)

        pieChartView.setOnCategoryClickListener { entry ->
            val categoryAmount = this.resources.getString(
                R.string.total_amount_center, "%.2f".format(entry.value)
            )
            val totalText = entry.category + " " + categoryAmount
            pieChartView.setTotalText(totalText)
        }
    }

    private fun parsePayload(jsonString: String): List<PieEntry> {
        val jsonParser = Json { ignoreUnknownKeys = true }

        val transactions: List<TransactionJson> = jsonParser.decodeFromString(jsonString)

        val categorySums = mutableMapOf<String, Float>()

        transactions.forEach { transaction ->
            val category = transaction.category
            val amount = transaction.amount
            categorySums[category] = (categorySums[category] ?: 0f) + amount
        }

        // Преобразуем в список пар: категория - сумма
        return categorySums.map { PieEntry(it.value, it.key) }
    }
}