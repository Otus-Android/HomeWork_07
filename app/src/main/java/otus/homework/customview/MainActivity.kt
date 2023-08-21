package otus.homework.customview

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(), PieChartView.Callbacks {

    private lateinit var pieChart: PieChartView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        pieChart = findViewById(R.id.PieChartView)
        //DataRepository.initialize()
        val filename = "payload.json"
        val jsonText = FileUtils.AssetsLoader.loadTextFromAsset(applicationContext, filename)
        val dataList = FileUtils.AssetsLoader.getDataFromText(jsonText)
        val categoryList = dataList.groupingBy { it.category }
            .reduce { _, acc, element ->
                PayLoadModel(0, "", acc.amount + element.amount, acc.category)
            }
            .values.toList()
        updateUi(dataList = categoryList)
    }

    private fun updateUi(dataList: List<PayLoadModel>) {
        pieChart.setDataChart(dataList)
        pieChart.startAnimation()
    }

    override fun onSectorSelected(valueModel: BaseValueModel) {
        Log.d("MA", "sector ${(valueModel as PayLoadModel).category} clicked")
    }
}