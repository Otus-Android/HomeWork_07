package otus.homework.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.google.gson.Gson
import java.io.FileReader
import android.content.res.Resources
import android.widget.Toast
import android.util.Log


class MainActivity : AppCompatActivity(), PieChartTouchListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val pieChart = findViewById<PieChart>(R.id.pieChart)
        val expencesList = readJson()
        val pieList = doPieItems(expencesList)

        findViewById<Button>(R.id.showBtn).setOnClickListener {
            pieChart?.setValues(pieList)
        }
        pieChart?.setPieChartTouchListener(this)
    }

    fun doPieItems(expenceList: Array<Expence>): List<PieItem> {
        // здесь храним иконки
        val catMap: MutableMap<String, PieItem> = mutableMapOf()
        for (expence in expenceList) {
            if (!catMap.containsKey(expence.category)) {
                catMap.put(expence.category, expence.toPieItem())
            } else {
                catMap.getValue(expence.category).value += expence.amount
            }
        }
        return catMap.map{it.value}
    }

    fun Resources.getRawTextFile(id: Int) =
        openRawResource(id).bufferedReader().use { it.readText() }

    fun readJson(): Array<Expence> {
        val fileContent = resources.getRawTextFile(R.raw.payload)
        var gson = Gson();
        return gson?.fromJson(fileContent, Array<Expence>::class.java)
    }

    override fun onPieItemClick(pieItem: PieItem) {
        val catName = pieItem?.name ?: "неизвестная категория"
        val catPercent = pieItem?.value.toString() ?: "-"
        findViewById<TextView>(R.id.txtComment).text =
            getString(R.string.catInfo, catName, catPercent)
    }

}