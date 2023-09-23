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
import otus.homework.customview.linechart.LineChartItem
import otus.homework.customview.linechart.LineChartView
import otus.homework.customview.piechart.PieChartItem
import otus.homework.customview.piechart.PieChartView
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private val payloadData: List<PayloadData> by readPayloadData()
    private val pieChartItemList: List<PieChartItem> by lazy {
        mapPayloadDataToPieChartItem(payloadData)
    }
    private val lineChartItemList: List<LineChartItem> by lazy {
        mapPayloadDataToLineChartItem(payloadData)
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

        val lineChartView = findViewById<LineChartView>(R.id.line_chart_view)

        findViewById<Spinner>(R.id.custom_view_variant).apply {
            adapter =
                createSpinnerAdapter()
            onItemSelectedListener =
                createSpinnerClickListener(pieChartView, lineChartView)
        }

        if (savedInstanceState == null) {
            pieChartView.setPieChartItems(pieChartItemList)
            lineChartView.setLineChartItems(lineChartItemList)
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

    private fun mapPayloadDataToLineChartItem(
        payloadData: List<PayloadData>
    ): List<LineChartItem> {
        return payloadData.map {
            LineChartItem(
                category = it.category,
                amount = it.amount,
                time = it.time
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

    private fun createSpinnerClickListener(
        pieChartView: PieChartView,
        lineChartView: LineChartView
    ): AdapterView.OnItemSelectedListener {
        return object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                itemSelected: View?,
                selectedItemPosition: Int,
                selectedId: Long
            ) {
                if (selectedItemPosition == 0) {
                    enablePieChartView()
                } else {
                    enableLineChartView()
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) = Unit

            private fun enablePieChartView() {
                pieChartView.visibility = ViewGroup.VISIBLE
                lineChartView.visibility = ViewGroup.GONE
            }

            private fun enableLineChartView() {
                pieChartView.visibility = ViewGroup.GONE
                lineChartView.visibility = ViewGroup.VISIBLE
            }
        }
    }
}