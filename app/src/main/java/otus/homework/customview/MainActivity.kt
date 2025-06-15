package otus.homework.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import otus.homework.customview.task_1.PieChartBogdan
import otus.homework.customview.util.getData


class MainActivity : AppCompatActivity() {

    private lateinit var customVIewBogdan: PieChartBogdan
    private lateinit var textView: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        customVIewBogdan = findViewById(R.id.custom)
        textView = findViewById(R.id.text)
        customVIewBogdan.setData(getData(this, R.raw.payload))
        customVIewBogdan.pressOnSector = {category ->
            textView.text = category.categoryName
        }
    }
}