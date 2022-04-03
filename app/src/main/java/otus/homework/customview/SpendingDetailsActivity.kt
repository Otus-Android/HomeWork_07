package otus.homework.customview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import javax.inject.Inject

class SpendingDetailsActivity : AppCompatActivity() {

    @Inject
    lateinit var repository: SpendingRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spending_details)
        (application as AppDelegate).appComponent.inject(this)

        val spendingGraph = findViewById<SpendingLineGraph>(R.id.spending_graph)
        spendingGraph.setData(repository.getCategoriesSpendingPerDate())
    }
}