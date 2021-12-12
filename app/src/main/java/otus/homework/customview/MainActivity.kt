package otus.homework.customview


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
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

        if (payments != null) {
            val paymentPieChart = getPaymentPieChart(payments) as List<PaymentPieChart>
            customViewPieChart.apply {
                this.setValue(paymentPieChart)
            }
        }

        lifecycleScope.launchWhenStarted {
            customViewPieChart.pieChartFlow
                .onEach {
                    if (it.isNotEmpty())
                        Toast.makeText(this@MainActivity, it, Toast.LENGTH_SHORT)
                        .show()
                }
                .collect()
        }
    }
}

private fun getPaymentPieChart(payments: List<Payment>): MutableList<PaymentPieChart> {
    var amountSum = 0
    payments.forEach {
        amountSum += it.amount
    }

    val category = mutableSetOf<String>()
    val listPaymentPieChart = mutableListOf<PaymentPieChart>()

    payments.forEach {
        category.add(it.category)
    }

    var position = 0
    while (position < category.size) {
        var sumAcc = 0
        val itemCategory = category.elementAt(position)

        payments.forEach {
            if (it.category == itemCategory) {
                sumAcc += it.amount
            }
        }
        val arc = sumAcc.toFloat() / (amountSum / 360)
        listPaymentPieChart.add(PaymentPieChart(sumAcc, itemCategory, arc))

        position++
    }
    return listPaymentPieChart
}
