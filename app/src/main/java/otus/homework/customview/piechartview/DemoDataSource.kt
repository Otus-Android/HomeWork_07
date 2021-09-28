package otus.homework.customview.piechartview

import android.content.Context
import org.json.JSONArray
import otus.homework.customview.R
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.Reader
import java.io.StringWriter
import java.io.Writer

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
