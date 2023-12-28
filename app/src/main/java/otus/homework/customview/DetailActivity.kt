package otus.homework.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import otus.homework.customview.pojo.Details
import otus.homework.customview.pojo.Mode

class DetailActivity : AppCompatActivity() {

    private val chartViewModel by lazy {
        ViewModelProvider(this)[ChartViewModel::class.java]
    }

    private var category: String? = null
    private var detailsCategoryByDate: List<Details>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val detailsView = layoutInflater.inflate(R.layout.activity_detail, null) as DetailsView
        setContentView(detailsView)

        intent.extras?.apply {
            category = getString(MainActivity.CATEGORY)
            //detailsCategoryByDate = getParcelableArrayList(MainActivity.DETAILS)
        }
        //category?.let { chartViewModel.showDetailsCategory(it) }

        chartViewModel.mode.observe(this) {
            when(it) {
                is Mode.DetailsCategory -> detailsView.populate(it.detailsData)
                else -> {}
            }
        }
    }
}