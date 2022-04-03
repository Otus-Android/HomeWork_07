package otus.homework.customview

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var repository: SpendingRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        (application as AppDelegate).appComponent.inject(this)

        val pieChart = findViewById<PieChartView>(R.id.pie_chart)
        pieChart.setData(repository.getCategoriesOverallSpending())
        pieChart.setSectorClickListener(object : PieChartView.OnSectorClickListener {
            override fun onSectorSelect(category: CategoryOverallSpending) {
                CategorySpendingBottomSheet.newInstance(category)
                    .show(supportFragmentManager, CategorySpendingBottomSheet.TAG)
            }
        })

        findViewById<Button>(R.id.btn_detailed_graph).setOnClickListener {
            startActivity(Intent(this, SpendingDetailsActivity::class.java))
        }
    }
}