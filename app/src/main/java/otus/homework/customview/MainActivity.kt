package otus.homework.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.google.gson.Gson
import android.content.res.Resources


class MainActivity : AppCompatActivity(), PieChartTouchListener {
    private lateinit var expencesList: Array<Expence>
    private var detailsGraph: DetailsGraph? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val pieChart = findViewById<PieChart>(R.id.pieChart)
        expencesList = readJson()
        val pieItems = doPieItems(expencesList)
        detailsGraph = findViewById<DetailsGraph>(R.id.detailsGraph)
        pieChart?.setValues(pieItems)
        pieChart?.setPieChartTouchListener(this)
    }

    fun doPieItems(expenceList: Array<Expence>): List<PieItem> {

        val catMap: MutableMap<String, PieItem> = mutableMapOf()
        for (expence in expenceList) {
            if (!catMap.containsKey(expence.category)) {
                catMap.put(expence.category, expence.toPieItem())
            } else {
                catMap.getValue(expence.category).value += expence.amount
            }
        }
        return catMap.map { it.value }
    }

    fun Resources.getRawTextFile(id: Int) =
        openRawResource(id).bufferedReader().use { it.readText() }

    fun readJson(): Array<Expence> {
        val fileContent = resources.getRawTextFile(R.raw.payload)
        return Gson().fromJson(fileContent, Array<Expence>::class.java)
    }

    /**
     * при клике на категорию в пайчарте показываем детализацию расходов для категории
     */
    override fun onPieItemClick(item: PieItem) {
        val catName = item.name
        val catPercent = item.value.toString()
        val catColor = item.color
        findViewById<TextView>(R.id.txtComment).text =
            getString(R.string.catInfo, catName, catPercent)

        val categoryExpences = expencesList.filter { it.category == catName }
        detailsGraph?.setColor(catColor)
        detailsGraph?.setValues(categoryExpences)
    }
}