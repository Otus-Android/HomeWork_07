package otus.homework.customview

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import otus.homework.customview.databinding.ActivityMainBinding
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.Comparator


class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding
        get() = _binding ?: throw RuntimeException("binding = null")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val expenses = getExpensesFromJson()
        if (expenses != null) {
            val allExpenses = AllExpenses(expenses)
            val items = mutableListOf<Item>()
            var total = 0
            val map = mutableMapOf<String, Int>()

            expenses.forEach {
                if (!map.containsKey(it.category)) map[it.category] = 0
                map[it.category] = map[it.category]!! + it.amount
                total += it.amount
            }
            for ((cat, amount) in map.entries) {
                items.add(Item(cat, amount))
            }

            val comparatorByAmount =
                Comparator<Item> { o1, o2 -> if (o1.amount > o2.amount) 1 else if (o2.amount > o1.amount) -1 else 0 }

            val itemList = ItemList(
                items.sortedWith(comparatorByAmount),
                total,
            )

            val allExpensesDayByDay = getDetailedExpensesByCategory(allExpenses)
            binding.myCustomDetailsGraph.setValues(
                allExpensesDayByDay
            )

            binding.myCustomView.apply {
                setValues(itemList)
                chooseCategoryCallback =
                    { item ->
                        val category = item.name
                        val list = getDetailedExpensesByCategory(allExpenses, category)
                        binding.myCustomDetailsGraph.setValues(
                            list
                        )
                    }
                switchCatsCallback = {
                    binding.myCustomDetailsGraph.setValues(
                        allExpensesDayByDay
                    )
                }
            }
        }
    }

    private fun getDetailedExpensesByCategory(
        allExpenses2: AllExpenses,
        category: String = ""
    ): List<Int> {
        Log.i("Category", "cat= $category, ${allExpenses2.mapByCategory[category]}")
        val categoryExpensesDetails = if (category == "") {
            allExpenses2.getAllCategoriesExpenses()
        } else {
            allExpenses2.getOneCategoryExpenses(category)
        }
        val byDay = mutableListOf<Int>()
        return if (categoryExpensesDetails.isNotEmpty()) {
            categoryExpensesDetails.forEach {
                byDay.add(it.amount)
            }
            byDay
        } else {
            emptyList()
        }

    }

    private fun getExpensesFromJson(): List<Expense>? {
        val inputStream = this.resources.openRawResource(R.raw.payload)
        val jsonStr = jsonToString(inputStream)
        val typeToken = object : TypeToken<List<Expense>>() {}.type
        return Gson().fromJson(jsonStr, typeToken)
    }

    private fun jsonToString(inputStream: InputStream) = try {
        val reader = BufferedReader(InputStreamReader(inputStream))
        val str = StringBuilder()
        var line = reader.readLine()
        while (line != null) {
            str.append(line)
            line = reader.readLine()
        }
        str.toString()

    } catch (e: Throwable) {
        ""
    } finally {
        inputStream.close()
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
