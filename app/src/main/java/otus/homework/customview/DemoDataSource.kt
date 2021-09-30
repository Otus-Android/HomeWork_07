package otus.homework.customview

import android.content.Context
import org.json.JSONArray
import otus.homework.customview.graphicview.GraphicBoundsModel
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.Reader
import java.io.StringWriter
import java.io.Writer
import java.util.Calendar

/**
 *
 *
 * @author Юрий Польщиков on 27.09.2021
 */
class DemoDataSource {

    fun getData(context: Context): List<Spending> {
        val spendingByCategory = LinkedHashMap<String, RawSpending>()
        var total = 0.0

        val jsonArray = JSONArray(readDemoFile(context))
        for (i in 0 until jsonArray.length()) {
            val item = jsonArray.getJSONObject(i)
            val rawCategory = item.getString("category")
            val amount = item.getDouble("amount")
            total += amount

            if (spendingByCategory[rawCategory] == null) {
                spendingByCategory[rawCategory] = RawSpending(
                    amount = amount,
                    category = Category.from(rawCategory),
                )
            } else {
                val oldAmount = spendingByCategory[rawCategory]!!.amount
                spendingByCategory[rawCategory] = spendingByCategory[rawCategory]!!.copy(amount = oldAmount + amount)
            }
        }

        val result = ArrayList<Spending>()
        for (categorySpending in spendingByCategory.values) {
            result.add(
                Spending(
                    amount = categorySpending.amount,
                    category = categorySpending.category,
                    percent = categorySpending.amount / total
                )
            )
        }
        return result
    }

    fun getDataForOneCategory(context: Context, category: Category): GraphicBoundsModel {
        val jsonArray = JSONArray(readDemoFile(context))

        var minTime = Long.MAX_VALUE
        var maxTime = Long.MIN_VALUE
        var minAmount = Double.MAX_VALUE
        var maxAmount = Double.MIN_VALUE

        val spendings = ArrayList<Spending>()
        for (i in 0 until jsonArray.length()) {
            val item = jsonArray.getJSONObject(i)
            val rawCategory = item.getString("category")
            if (category.title == rawCategory) {
                val amount = item.getDouble("amount")
                if (amount < minAmount) {
                    minAmount = amount
                }
                if (amount > maxAmount) {
                    maxAmount = amount
                }

                val name = item.getString("name")

                val time = item.getLong("time")
                if (time < minTime) {
                    minTime = time
                }
                if (time > maxTime) {
                    maxTime = time
                }

                val calendar = Calendar.getInstance().apply {
                    this.timeInMillis = time
                }
                spendings.add(
                    Spending(
                        amount = amount,
                        category = category,
                        name = name,
                        time = calendar
                    )
                )
            }
        }
        val minDate = Calendar.getInstance().apply {
            timeInMillis = minTime
        }
        val maxDate = Calendar.getInstance().apply {
            timeInMillis = maxTime
        }
        return GraphicBoundsModel(minDate, maxDate, minAmount, maxAmount, spendings)
    }

    private fun readDemoFile(context: Context): String {
        val raw = context.resources.openRawResource(R.raw.payload)
        val writer: Writer = StringWriter()
        val buffer = CharArray(1024)
        raw.use { rawData ->
            val reader: Reader = BufferedReader(InputStreamReader(rawData, "UTF-8"))
            var n: Int
            while (reader.read(buffer).also { n = it } != -1) {
                writer.write(buffer, 0, n)
            }
        }
        return writer.toString()
    }

    private data class RawSpending(
        val amount: Double,
        val category: Category,
    )
}
