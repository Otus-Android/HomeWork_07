package otus.homework.customview

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        val pieChartView: PieChartView = findViewById(R.id.pieChart)
//        pieChartView.onClickListener = {
//            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
//        }

        findViewById<LineChartView>(R.id.LineChartView).setData(getDataItemList(this)!!)
    }
}