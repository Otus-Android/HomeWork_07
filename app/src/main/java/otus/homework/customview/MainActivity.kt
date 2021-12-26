package otus.homework.customview


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.squareup.moshi.Moshi
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

private const val TAG = "debug"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val raw = resources.openRawResource(R.raw.payload).bufferedReader().use { it.readText() }

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val listType = Types.newParameterizedType(List::class.java, Payment::class.java)
        val jsonAdapter: JsonAdapter<List<Payment>> = moshi.adapter(listType)
        val payments: List<Payment>? = jsonAdapter.fromJson(raw)

        val customViewPieChart = findViewById<CustomViewPieChart>(R.id.customViewPieChart)
        val customViewLineGraph = findViewById<CustomViewLineGraph>(R.id.lineGraph)
        val paymentLineGraph = mutableListOf<PaymentLineGraph>()

        if (payments != null) {
            val paymentPieChart = getPaymentPieChart(payments) as List<PaymentPieChart>
            customViewPieChart.apply {
                this.setValue(paymentPieChart)
            }
        }

        lifecycleScope.launchWhenStarted {
            customViewPieChart.pieChartFlow
                .onEach { category ->
                    if (category.isNotEmpty())
                        customViewLineGraph.apply {
                            if (payments != null) {
                                paymentLineGraph.clear()
                                payments.forEach {
                                paymentLineGraph.add(PaymentLineGraph(it.amount, it.category, it.time))
                                }
                                setValue(
                                    paymentLineGraph
                                    .filter{ it.category == category}
                                    .sortedBy{it.date}
                                )
                            }
                        }
                }
                .collect()
        }
    }
}

private fun getPaymentPieChart(payments: List<Payment>): MutableList<PaymentPieChart> {

    val amountSum = payments.sumOf { it.amount }
    val category = payments.map{ it.category  }.distinct()
    val listPaymentPieChart = mutableListOf<PaymentPieChart>()

    category.forEachIndexed { _, item ->
        var acc = 0
        payments.forEach {
            if (it.category == item) {
               acc += it.amount
            }
        }
        listPaymentPieChart.add(PaymentPieChart
            (acc, item, acc.toFloat() / (amountSum / 360)
            )
        )
    }
    return listPaymentPieChart
}
