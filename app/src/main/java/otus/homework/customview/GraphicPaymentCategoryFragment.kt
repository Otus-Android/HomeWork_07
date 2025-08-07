package otus.homework.customview

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.transition.MaterialFadeThrough
import kotlinx.serialization.json.Json
import otus.homework.customview.model.TransactionJson
import otus.homework.customview.view.GraphicPaymentCategoryView
import java.util.Calendar

class GraphicPaymentCategoryFragment : Fragment(R.layout.fragment_graphic_payment_category) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val pieChartView = view.findViewById<GraphicPaymentCategoryView>(R.id.graphic)
        val jsonString =
            this.resources.openRawResource(R.raw.payload).bufferedReader().use { it.readText() }

        pieChartView.setCategoryData(parsePayload(jsonString))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough().apply {
            duration = FRAGMENT_ANIMATE_DURATION
        }
        exitTransition = MaterialFadeThrough().apply {
            duration = FRAGMENT_ANIMATE_DURATION
        }
    }

    private fun parsePayload(
        jsonString: String
    ): Map<String, List<GraphicPaymentCategoryView.CategoryPoint>> {
        val jsonParser = Json { ignoreUnknownKeys = true }

        val transactions: List<TransactionJson> = jsonParser.decodeFromString(jsonString)

        return transactions
            .map { transaction ->
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = transaction.time * 1000
                }
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                transaction.category to GraphicPaymentCategoryView.CategoryPoint(
                    day,
                    transaction.amount
                )
            }
            .groupBy(
                keySelector = { it.first },
                valueTransform = { it.second }
            )
    }

    companion object {
        private const val FRAGMENT_ANIMATE_DURATION = 300L
    }

}