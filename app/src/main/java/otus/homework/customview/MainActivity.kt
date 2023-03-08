package otus.homework.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {

  private val chartView: PieChartView by lazy { findViewById(R.id.chart_view) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val model = PieChartModel(
      listOf(
        PieChartDto(1, "", 10, "", 0),
        PieChartDto(2, "", 20, "", 0),
        PieChartDto(3, "", 30, "", 0),
        PieChartDto(4, "", 40, "", 0),
      )
    )
    chartView.model = model
  }
}