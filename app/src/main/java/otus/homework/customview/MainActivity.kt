package otus.homework.customview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    val listPayload = readJsonFromRaw()
    val pieChart = findViewById<PieChartView>(R.id.view_pie_chart)
    pieChart.setValues(listPayload)
    val categoriesChart = findViewById<CategoriesChartView>(R.id.view_categories_chart)
    categoriesChart.setCategories(listPayload)
  }

  private fun readJsonFromRaw(): List<PayloadUiModel> {
    val inputStream = this.resources.openRawResource(R.raw.payload)
    val jsonString = inputStream.bufferedReader().use { it.readText() }
    val listType: Type = object : TypeToken<List<PayloadUiModel>>() {}.type
    return Gson().fromJson(jsonString, listType)
  }
}
