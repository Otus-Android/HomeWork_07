package otus.homework.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val products = loadData()
        val palette = Palette()
        palette.mapColors(products.map { it.category }.distinct())

        val pieChartView = findViewById<PieChartView>(R.id.pieChartView)
        pieChartView.setContent(products, palette)

        val lineChartView = findViewById<LineChartView>(R.id.lineChartView)
        lineChartView.setContent(products, palette)
    }

    private fun loadData() : List<ProductData> {
        val file = resources.openRawResource(R.raw.payload)
        val content = file.readBytes().decodeToString()
        file.close()
        val type = object : TypeToken<List<ProductData>>() {}.type
        return Gson().fromJson(content, type)
    }
}