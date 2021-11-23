package otus.homework.customview

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.w3c.dom.Text
import otus.homework.customview.graph.GraphView
import otus.homework.customview.graph.Point
import otus.homework.customview.paychart.PayChartView
import java.util.SortedMap

class MainActivity : AppCompatActivity() {
    private lateinit var graphView: GraphView
    private lateinit var titleTv: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val purchaseList = getPayload(this)

        if (savedInstanceState == null) {
            val piecesOfCake = mapPurchases(purchaseList)
            val pointList = mapPurchasesToPoints(purchaseList)
            findViewById<PayChartView>(R.id.pie).apply {
                setPieces(piecesOfCake)
                onSectorClickListener = { category ->
                    graphView.setPoints(pointList[category]!!)
                    titleTv.text = category
                }
            }
            graphView = findViewById(R.id.graph)
            titleTv = findViewById(R.id.graphName)
        }
    }

    private fun mapPurchasesToPoints(payments: List<Purchase>?) =
        mutableMapOf<String, List<Point>>().apply {
            payments?.groupBy { it.category }?.onEach {  entry ->
                put(
                    entry.key,
                    entry.value.map { Point(it.time.toFloat(), it.amount.toFloat()) }
                )
            }
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
