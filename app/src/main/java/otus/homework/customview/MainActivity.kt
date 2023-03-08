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
        PieChartDto(5, "", 40, "", 0),
        PieChartDto(6, "", 40, "", 0),
        PieChartDto(7, "", 40, "", 0),
        PieChartDto(8, "", 40, "", 0),
        PieChartDto(9, "", 40, "", 0),
        PieChartDto(10, "", 40, "", 0),
        PieChartDto(11, "", 40, "", 0),
        PieChartDto(12, "", 40, "", 0),
        PieChartDto(13, "", 40, "", 0),
      )
    )
    chartView.model = model
  }
}