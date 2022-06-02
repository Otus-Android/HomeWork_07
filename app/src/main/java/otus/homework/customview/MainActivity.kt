package otus.homework.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import otus.homework.customview.lineargraph.LinearGraphView
import otus.homework.customview.lineargraph.Point
import otus.homework.customview.piechart.PiechartView
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var graphView: LinearGraphView
    private lateinit var titleTv: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val purchaseList = getPayload(this)

        if (savedInstanceState == null) {
            val piecesOfCake = mapPurchases(purchaseList)
            val pointList = mapPurchasesToPoints(purchaseList)
            findViewById<PiechartView>(R.id.piechart_view).apply {
                setPieces(piecesOfCake)
                onSectorClickListener = { category ->
                    graphView.setPoints(pointList[category]!!)
                    titleTv.text = category
                }
            }
            graphView = findViewById(R.id.lineargraph_view)
            titleTv = findViewById(R.id.category_tv)
        }
    }

    private fun mapPurchasesToPoints(payments: List<Purchase>?) =
        mutableMapOf<String, List<Point>>().apply {
            payments?.groupBy { it.category }?.onEach { entry ->
                put(
                    entry.key,
                    entry.value.map { Point(it.time.toFloat(), it.amount.toFloat()) }
                )
            }
        }

    private fun mapPurchases(payments: List<Purchase>?): SortedMap<String, Float> =
        sortedMapOf<String, Float>().apply {
            payments?.groupBy { it.category }?.onEach { entry ->
                put(
                    entry.key, entry.value.sumOf { it.amount }.toFloat()
                )
            }
        }
}