package otus.homework.customview

import android.os.Bundle
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import otus.homework.customview.linear.LinearChart
import otus.homework.customview.pie.PieCartCallback
import otus.homework.customview.pie.PieChart
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    init {
        Timber.plant(Timber.DebugTree())
    }

    var pieChart: PieChart? = null
    var linearChart: LinearChart? = null

    private val pieCharCallback = object : PieCartCallback {
        override fun sectorClick(category: String) {
            findViewById<TextView>(R.id.pieChartText).text = category
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val repository = SpendRepository(this)

        pieChart = findViewById<PieChart>(R.id.pieChart).apply {
            setCallback(pieCharCallback)
        }

        linearChart = findViewById<LinearChart>(R.id.linearChart).apply {

        }

        if (savedInstanceState == null) {
            pieChart?.setItems(repository.getSpendItems())
            pieChart?.startAnimationRotation()

        }

        //linear chart

        val linearChartItems = repository.getSpendItemsLinear()

        val menuItems = linearChartItems.map { it.category }.distinct()

        val popupMenu = PopupMenu(this, linearChart).apply {
            menuItems.forEach {
                menu.add(it)
            }
        }.apply {
            setOnMenuItemClickListener { menuItem ->
                val items = linearChartItems.filter {
                    it.category == menuItem.title
                }

                linearChart?.setData(items)

                true
            }
        }

        linearChart?.setOnClickListener {
            popupMenu.show()
        }
    }

    override fun onDestroy() {
        pieChart?.removeCallback()

        super.onDestroy()
    }
}
