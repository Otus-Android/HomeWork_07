package otus.homework.customview

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import otus.homework.customview.common.PayloadData
import otus.homework.customview.piechart.PieChartItem
import otus.homework.customview.piechart.PieChartView
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private val payloadData: List<PayloadData> by readPayloadData()
    private val pieChartItemList: List<PieChartItem> by lazy {
        mapPayloadDataToPieChartItem(payloadData)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val pieChartView = findViewById<PieChartView>(R.id.pie_chart_view).apply {
            setOnSectionClickListener { section ->
                val totalAmount = pieChartItemList.sumOf { it.amount }
                val sectionAmount = section.pieChartItems.sumOf { it.amount }.toFloat()
                setCenterText("${(sectionAmount / totalAmount * 100).roundToInt()}%")
            }
        }

        findViewById<Spinner>(R.id.custom_view_variant).apply {
            adapter = createSpinnerAdapter()
            onItemSelectedListener = createSpinnerClickListener(pieChartView)
        }

        if (savedInstanceState == null) {
            pieChartView.setPieChartItems(pieChartItemList)
        }
    }

    private fun mapPayloadDataToPieChartItem(
        payloadData: List<PayloadData>
    ): List<PieChartItem> {
        return payloadData.map {
            PieChartItem(
                name = it.name,
                category = it.category,
                amount = it.amount
            )
        }
    }

    private fun readPayloadData(): Lazy<List<PayloadData>> {
        return lazy {
            val fileData = resources.openRawResource(R.raw.payload).reader().readText()
            Json.decodeFromString(fileData)
        }
    }

    private fun createSpinnerAdapter(): ArrayAdapter<CharSequence> {
        return ArrayAdapter.createFromResource(
            this,
            R.array.custom_view_variants,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
    }

    private fun createSpinnerClickListener(view: PieChartView): AdapterView.OnItemSelectedListener {
        return object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                itemSelected: View?,
                selectedItemPosition: Int,
                selectedId: Long
            ) {
                when (selectedItemPosition) {
                    0 -> view.visibility = ViewGroup.VISIBLE
                    else -> view.visibility = ViewGroup.GONE
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }
}