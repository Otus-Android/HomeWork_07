package otus.homework.customview

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity(), PieChartView.Callbacks {

    private lateinit var pieChart: PieChartView
    private lateinit var lineChart: BarChartView
    private lateinit var buttonBack: MaterialButton
    private var dataList: List<PayLoadModel> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        pieChart = findViewById(R.id.pieChartView)
        lineChart = findViewById(R.id.barChartView)
        lineChart.visibility = View.GONE
        buttonBack = findViewById(R.id.buttonBack)

        buttonBack.apply {
            setOnClickListener {
                goBack()
            }
        }

        val filename = "payload.json"
        val jsonText = FileUtils.AssetsLoader.loadTextFromAsset(applicationContext, filename)
        dataList = FileUtils.AssetsLoader.getDataFromText(jsonText)
        val categoryList = dataList.groupingBy { it.category }
            .reduce { _, acc, element ->
                PayLoadModel(0, "", acc.amount + element.amount, acc.category)
            }
            .values.toList()

        updateUi(dataList = categoryList)

    }

    private fun updateUi(dataList: List<PayLoadModel>) {
        pieChart.setValues(dataList)
        pieChart.startAnimation()
    }

    override fun onSectorSelected(valueModel: BaseValueModel) {
        pieChart.visibility = View.GONE
        val category = (valueModel as PayLoadModel).category
        Log.d("MA", "sector $category clicked")
        val purchasesListByCategory =
            dataList.filter { it.category == category }
        lineChart.setValues(purchasesListByCategory)
        lineChart.visibility = View.VISIBLE
        buttonBack.visibility = View.VISIBLE
    }

    private fun goBack() {
        pieChart.visibility = View.VISIBLE
        lineChart.visibility = View.GONE
        buttonBack.visibility = View.GONE
    }

}