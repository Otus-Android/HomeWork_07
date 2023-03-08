package otus.homework.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

  private val chartView: PieChartView by lazy { findViewById(R.id.chart_view) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val items = listOf(
      PieChartDto(1, "1", 10, "", 0),
      PieChartDto(2, "2", 20, "", 0),
      PieChartDto(3, "3", 30, "", 0),
      PieChartDto(4, "4", 40, "", 0),
      PieChartDto(5, "5", 40, "", 0),
      PieChartDto(6, "6", 40, "", 0),
      PieChartDto(7, "7", 40, "", 0),
      PieChartDto(8, "8", 40, "", 0),
      PieChartDto(9, "9", 40, "", 0),
      PieChartDto(10, "10", 40, "", 0),
      PieChartDto(11, "11", 40, "", 0),
      PieChartDto(12, "12", 40, "", 0),
      PieChartDto(13, "13", 40, "", 0),
    )
    val model = PieChartModel(
      items.map { PieChartItem(it.name, it.amount) }
    )
    chartView.model = model

    var index = 0
    thread {
      while (true) {
        Thread.sleep(200)
        chartView.post { chartView.setAccentSection(++index % items.size) }
      }
    }
  }
}
