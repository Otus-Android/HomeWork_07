package otus.homework.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import otus.homework.customview.ui.PieChartItem
import otus.homework.customview.ui.PieChartModel
import otus.homework.customview.ui.PieChartView

class MainActivity : AppCompatActivity() {

  private val chartView: PieChartView by lazy { findViewById(R.id.chart_view) }

  private val items = listOf(
    PieChartDto(1, "1", 10, "A", 0),
    PieChartDto(2, "2", 20, "B", 0),
    PieChartDto(3, "3", 30, "C", 0),
    PieChartDto(4, "4", 40, "D", 0),
    PieChartDto(5, "5", 40, "A", 0),
    PieChartDto(6, "6", 40, "B", 0),
    PieChartDto(7, "7", 40, "C", 0),
    PieChartDto(8, "8", 40, "D", 0),
    PieChartDto(9, "9", 40, "A", 0),
    PieChartDto(10, "10", 40, "B", 0),
    PieChartDto(11, "11", 40, "C", 0),
    PieChartDto(12, "12", 40, "D", 0),
    PieChartDto(13, "13", 40, "A", 0),
  )

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    showItems()

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
}
