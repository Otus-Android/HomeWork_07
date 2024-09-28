package otus.homework.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ToggleButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val receipts = loadData()
        val lineChartView = findViewById<LineChartView>(R.id.lineChartView)
        lineChartView.setData(receipts)
        val pieChartView = findViewById<PieChartView>(R.id.pieChartView)
        pieChartView.setData(receipts)
        pieChartView.setSelectionListener { selectedCategory ->
            lineChartView.setSelectedCategory(selectedCategory)
        }
        if(receipts.isNotEmpty())
            pieChartView.setSelectedCategory(receipts.get(0).category)

        lineChartView.setOnClickListener { lineChartView.switchSmoothing() }
    }
    private fun loadData() : List<ReceiptData> {
        val file =resources.openRawResource(R.raw.payload)
        val jsonString = file.readBytes().decodeToString()
        file.close()
        val listTutorialType = object : TypeToken<List<ReceiptData>>() {}.type
        val receipt: List<ReceiptData> = Gson().fromJson(jsonString, listTutorialType)
        return receipt
    }
}