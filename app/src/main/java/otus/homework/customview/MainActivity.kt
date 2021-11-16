package otus.homework.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import otus.homework.customview.paychart.PayChartView
import otus.homework.customview.paychart.Piece

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val purchaseList = getPayload(this)

        if (savedInstanceState == null) {
            val piecesOfCake = mapPurchases(purchaseList)
            findViewById<PayChartView>(R.id.pie).apply {
                setPieces(piecesOfCake)
                onSectorClickListener = {
                    Log.d("myTag", it.toString())
                }
            }
        }
    }
}

private fun mapPurchases(payments: List<Purchase>?): Set<Piece> =
    mutableSetOf<Piece>().apply {
        payments?.groupBy { it.category }?.onEachIndexed { index, entry ->
            add(
                Piece(index, entry.value.sumOf { it.amount }.toFloat())
            )
        }
    }
