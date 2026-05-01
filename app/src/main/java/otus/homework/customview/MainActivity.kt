package otus.homework.customview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import otus.homework.customview.databinding.ActivityMainBinding
import otus.homework.customview.models.Expense

/**
 * Главный экран приложения, отображающий круговую диаграмму расходов.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val expenses = loadExpenses()
        binding.pieChart.setData(expenses)
    }

    /**
     * Загружает список расходов из сырого ресурса payload.json.
     * 
     * @return Список объектов [Expense].
     */
    private fun loadExpenses(): List<Expense> {
        val jsonString = resources.openRawResource(R.raw.payload).bufferedReader().use { it.readText() }
        val type = object : TypeToken<List<Expense>>() {}.type
        return Gson().fromJson(jsonString, type)
    }
}
