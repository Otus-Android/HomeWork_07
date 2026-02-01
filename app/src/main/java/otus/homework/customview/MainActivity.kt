package otus.homework.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStream
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var pieChartView: PieChartView
    private lateinit var selectedCategoryText: TextView
    private lateinit var selectedAmountText: TextView
    private lateinit var selectedPercentageText: TextView

    companion object {
        private const val KEY_SELECTED_CATEGORY = "selected_category"
        private const val KEY_SELECTED_AMOUNT = "selected_amount"
        private const val KEY_SELECTED_PERCENTAGE = "selected_percentage"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pieChartView = findViewById(R.id.pieChartView)
        selectedCategoryText = findViewById(R.id.selectedCategoryText)
        selectedAmountText = findViewById(R.id.selectedAmountText)
        selectedPercentageText = findViewById(R.id.selectedPercentageText)

        // Загружаем данные из JSON
        val expenses = loadExpensesFromJson()

        // Устанавливаем данные в график
        pieChartView.setData(expenses)

        // Восстанавливаем состояние если есть
        if (savedInstanceState != null) {
            restoreState(savedInstanceState)
        } else {
            // Или используем состояние из PieChartView
            pieChartView.getSelectedCategory()?.let { category ->
                updateCategoryInfo(category)
            }
        }

        // Устанавливаем коллбек
        pieChartView.onCategoryClickListener = { category ->
            updateCategoryInfo(category)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // Сохраняем выбранную категорию
        pieChartView.getSelectedCategory()?.let { category ->
            outState.putString(KEY_SELECTED_CATEGORY, category.category)
            outState.putDouble(KEY_SELECTED_AMOUNT, category.totalAmount)
            outState.putFloat(KEY_SELECTED_PERCENTAGE, category.percentage)
        }
    }

    private fun restoreState(savedInstanceState: Bundle?) {
        val categoryName = savedInstanceState?.getString(KEY_SELECTED_CATEGORY)
        val amount = savedInstanceState?.getDouble(KEY_SELECTED_AMOUNT)
        val percentage = savedInstanceState?.getFloat(KEY_SELECTED_PERCENTAGE)

        if (categoryName != null) {
            // Создаем временный объект категории
            val category = CategoryData(
                category = categoryName,
                totalAmount = amount ?: 0.0,
                percentage = percentage ?: 0f,
                color = 0
            )

            // Обновляем UI
            updateCategoryInfo(category)

            // Сообщаем PieChartView о выбранной категории
            pieChartView.setSelectedCategory(categoryName)
        }
    }

    private fun updateCategoryInfo(category: CategoryData) {
        selectedCategoryText.text = getString(R.string.category_template, category.category)
        selectedAmountText.text = getString(R.string.amount_template, category.totalAmount)
        selectedPercentageText.text = getString(R.string.percentage_template, category.percentage.toInt())
    }

    private fun loadExpensesFromJson(): List<Expense> {
        return try {
            val inputStream: InputStream = resources.openRawResource(R.raw.payload)
            val jsonString = inputStream.bufferedReader().use { it.readText() }

            val gson = Gson()
            val type = object : TypeToken<List<Expense>>() {}.type
            gson.fromJson(jsonString, type)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

}