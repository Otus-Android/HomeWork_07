package otus.homework.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import otus.homework.customview.dto.DomainDto
import otus.homework.customview.ui.*

class MainActivity : AppCompatActivity() {

  private val chartView: PieChartView by lazy { findViewById(R.id.chart_view) }
  private val linesView: LinearChartView by lazy { findViewById(R.id.lines_view) }

  private var items = emptyList<DomainDto>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    items = Gson().fromJson(
      resources
        .openRawResource(R.raw.payload)
        .bufferedReader()
        .use { it.readText() },
      object : TypeToken<List<DomainDto>>() {}.type
    )

    if (savedInstanceState == null) {
      showItems()
      showLines()
    }

    findViewById<View>(R.id.button_items).setOnClickListener {
      showItems()
    }
    findViewById<View>(R.id.button_categories).setOnClickListener {
      showCategories()
    }
  }

  private fun showItems() {
    val model = PieChartModel(
      items.map { PieChartItem(it.name, it.amount) }
    )
    chartView.updateData(model)
  }

  private fun showCategories() {
    val categories = hashMapOf<String, Int>()
    items.forEach {
      if (categories.contains(it.category)) {
        categories[it.category] = categories[it.category]!! + it.amount
      } else {
        categories[it.category] = it.amount
      }
    }

    val model = PieChartModel(
      categories.map { PieChartItem(it.key, it.value) }
    )
    chartView.updateData(model)
  }

  private fun showLines() {
    val categories = items.map { it.category }.distinct()
    val lines = categories.map { cat ->
      val list = items.filter { it.category == cat }.map { LinearChartItem(it.amount, it.time) }
      cat to list
    }
    val model = LinearChartModel(lines)
    linesView.updateData(model)
  }
}
