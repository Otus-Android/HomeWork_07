package otus.homework.customview.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.CheckBox
import android.widget.Toast
import com.google.android.material.chip.ChipGroup
import com.google.android.material.slider.RangeSlider
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import alektas.views.pie_chart.PieChart
import alektas.views.pie_chart.PieChartItem
import otus.homework.customview.R
import otus.homework.customview.data.models.Purchase
import otus.homework.customview.ui.utils.ColorUtils
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val pieChart = findViewById<PieChart>(R.id.pieChart)

        initSettings(pieChart)

        val purchasesMap = loadPurchases()
        val purchasesChartItemMap = purchasesMap.mapToPieChartItems()
        val categories = purchasesChartItemMap.keys.toList()

        pieChart.doubleClickListener = { item ->
            Log.d("MainActivity", "onCreate: clicked item = $item")
            Toast.makeText(context, "Selected $item", Toast.LENGTH_SHORT).show()
            purchasesChartItemMap[item]?.let { purchases ->
                display(purchases, fromItem = item)
                return@let
            }

            if (item == null) display(categories)
        }

        pieChart.selectionListener = { item ->
            Log.d("MainActivity", "onCreate: selected item = $item")
        }

        pieChart.display(categories)
    }

    private fun initSettings(pieChart: PieChart) {
        val sortButtons = findViewById<ChipGroup>(R.id.cgSortType).apply {
            setOnCheckedChangeListener { _, checkedId ->
                val sort = when (checkedId) {
                    R.id.chipSortByAmount -> PieChart.Sort.BY_AMOUNT
                    R.id.chipSortById -> PieChart.Sort.BY_ID
                    R.id.chipSortByLabel -> PieChart.Sort.BY_LABEL
                    else -> return@setOnCheckedChangeListener
                }
                pieChart.sort = sort
            }
        }
        sortButtons.check(R.id.chipSortByAmount)

        val orderButtons = findViewById<ChipGroup>(R.id.cgOrderType).apply {
            setOnCheckedChangeListener { _, checkedId ->
                val order = when (checkedId) {
                    R.id.chipOrderAscending -> PieChart.Order.ASCENDING
                    R.id.chipOrderDescending -> PieChart.Order.DESCENDING
                    else -> return@setOnCheckedChangeListener
                }
                pieChart.order = order
            }
        }
        orderButtons.check(R.id.chipOrderAscending)

        val showLabelsCheckBox = findViewById<CheckBox>(R.id.cbShowLabels).apply {
            setOnCheckedChangeListener { _, isChecked ->
                pieChart.showLabels = isChecked
            }
        }
        showLabelsCheckBox.isChecked = true

        val preserveSizeCheckBox = findViewById<CheckBox>(R.id.cbPreserveCircleSize).apply {
            setOnCheckedChangeListener { _, isChecked ->
                pieChart.preserveCircleSize = isChecked
            }
        }
        preserveSizeCheckBox.isChecked = true

        val showDetailsPercentCheckBox = findViewById<CheckBox>(R.id.cbShowDetailsPercent).apply {
            setOnCheckedChangeListener { _, isChecked ->
                pieChart.showDetailsPercent = isChecked
            }
        }
        showDetailsPercentCheckBox.isChecked = true

        val showDetailsAmountCheckBox = findViewById<CheckBox>(R.id.cbShowDetailsAmount).apply {
            setOnCheckedChangeListener { _, isChecked ->
                pieChart.showDetailsAmount = isChecked
            }
        }
        showDetailsAmountCheckBox.isChecked = true

        findViewById<RangeSlider>(R.id.sliderSegmentWidth).apply {
            values = listOf(pieChart.unselectedSegmentWidth)
            addOnChangeListener { _, value, _ ->
                pieChart.unselectedSegmentWidth = value
            }
        }

        findViewById<RangeSlider>(R.id.sliderStartAngle).apply {
            values = listOf(pieChart.segmentStartAngle)
            addOnChangeListener { _, value, _ ->
                pieChart.segmentStartAngle = value
            }
        }
    }

    private fun loadPurchases(): Map<String, List<Purchase>> {
        val dataType = object : TypeToken<List<Purchase>>() {}.type
        return InputStreamReader(resources.openRawResource(R.raw.payload)).use { reader ->
            Gson().fromJson<List<Purchase>>(reader, dataType)
                .groupBy { it.category }
        }
    }

    private fun Map<String, List<Purchase>>.mapToPieChartItems(): Map<PieChartItem, List<PieChartItem>> =
        buildMap {
            this@mapToPieChartItems.forEach { (category, purchases) ->
                val purchaseChartItems = mutableListOf<PieChartItem>()
                val categoryAmount = purchases.fold(0) { acc, purchase ->
                    with(purchase) {
                        purchaseChartItems.add(
                            PieChartItem(
                                id,
                                name,
                                amount,
                                ColorUtils.randomColor()
                            )
                        )
                        acc + amount
                    }
                }
                val categoryChartItem = PieChartItem(
                    id = category.hashCode(),
                    label = category,
                    amount = categoryAmount,
                    segmentColor = ColorUtils.randomColor()
                )
                put(categoryChartItem, purchaseChartItems)
            }
        }

}