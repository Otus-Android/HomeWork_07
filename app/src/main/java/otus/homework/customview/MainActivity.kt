package otus.homework.customview

import android.animation.LayoutTransition
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.isInvisible
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import otus.homework.customview.databinding.ActivityMainBinding
import otus.homework.customview.model.Category
import otus.homework.customview.model.Purchase
import otus.homework.customview.pie_chart.model.PieConfigViewData

class MainActivity : AppCompatActivity() {

    private lateinit var colorsArray: IntArray

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)

        colorsArray = resources.getIntArray(R.array.chart_colors)

        val purchases = getPayload()

        if (savedInstanceState == null) {
            configChartPie(purchases)
        }
    }

    private fun configChartPie(purchases: List<Purchase>) {
        val categories = getCategories(purchases)
        val clickListener: (String?) -> Unit = { category ->
            if (category != null) {
                binding.chartDetail.setPayload(purchases.groupBy { purchase ->
                    purchase.category
                }[category] ?: emptyList())
                binding.chartDetail.isInvisible = false
            } else {
                binding.chartDetail.isInvisible = true
            }
        }
        binding.chart.config(
            PieConfigViewData(
                payload = categories,
                clickListener = clickListener,
                minSizePx = applicationContext.asPixel(100),
                innerSizePercent = 0.7f,
                animatedSizePercent = 1.2f
            )
        )
    }

    private fun getPayload(): List<Purchase> {
        val payloadString = resources
            .openRawResource(R.raw.payload)
            .bufferedReader()
            .use { it.readText() }
        return GsonBuilder().create().fromJson(
            payloadString,
            object : TypeToken<List<Purchase>>() {}.type
        )
    }

    private fun getCategories(purchases: List<Purchase>): List<Category> = purchases.groupBy {
        it.category
    }.entries.mapIndexed { index, entry ->
        Category(entry.key, colorsArray[index], entry.value.sumBy { it.amount })
    }

}