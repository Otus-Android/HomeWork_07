package otus.homework.customview

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import otus.homework.customview.models.Spend
import java.io.IOException
import java.io.InputStream

class MainActivity : AppCompatActivity() {
    private var index = 0

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val textView = findViewById<TextView>(R.id.tvCategory)
        val btnPrev = findViewById<Button>(R.id.btnPrev)
        val btnNext = findViewById<Button>(R.id.btnNext)
        val chart = findViewById<LineChartView>(R.id.chart)
        val list = readJson()
        val groupedItems = list.groupBy { it.category }

        btnPrev.apply {
            if (groupedItems.isEmpty()) {
                visibility = View.GONE
            } else {
                visibility = View.VISIBLE
                setOnClickListener {
                    if (index == 0) {
                        index = groupedItems.size-1
                    } else {
                        index--
                    }
                    chart.setSelectedIndex(index)
                    textView.text = groupedItems.keys.toList()[index]
                }
            }
        }
        btnNext.apply {
            if (groupedItems.isEmpty()) {
                visibility = View.GONE
            } else {
                visibility = View.VISIBLE
                setOnClickListener {
                    if (index == groupedItems.size-1) {
                        index = 0
                    } else {
                        index++
                    }
                    chart.setSelectedIndex(index)
                    textView.text = groupedItems.keys.toList()[index]
                }
            }
        }

        chart.setItems(groupedItems)
        if (groupedItems.isNotEmpty()) {
            chart.setSelectedIndex(index)
            textView.text = groupedItems.keys.toList()[index]
        }
    }

    private fun readJson(): List<Spend> {
        return try {
            var json: String? = null
            val inputStream: InputStream = resources.openRawResource(
                resources.getIdentifier(
                    "payload",
                    "raw", packageName
                )
            )

            json = inputStream.bufferedReader().use { it.readText() }
            val itemType = object : TypeToken<List<Spend>>() {}.type
            val gson = GsonBuilder().create()
            gson.fromJson<List<Spend>>(json, itemType)

        } catch (e: IOException) {
            emptyList()
        }
    }
}