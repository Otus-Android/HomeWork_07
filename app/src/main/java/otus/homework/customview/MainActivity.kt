package otus.homework.customview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import otus.homework.customview.task_1.model.Category
import otus.homework.customview.task_2.CategoryChartBogdan
import otus.homework.customview.util.getData
import java.util.*


class MainActivity : AppCompatActivity() {

    //    private lateinit var customVIewBogdan: PieChartBogdan
    private lateinit var categoryChartBogdan: CategoryChartBogdan

    //   private lateinit var textView: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        customVIewBogdan = findViewById(R.id.custom)
//        textView = findViewById(R.id.text)
//        customVIewBogdan.setData(getData(this, R.raw.payload))
//        customVIewBogdan.pressOnSector = {category ->
//            textView.text = category.categoryName
//        }

        categoryChartBogdan = findViewById(R.id.chart)
        categoryChartBogdan.setData(
            Category.HEALTH,
            getData(this, R.raw.payload),
            Calendar.JANUARY
        )
    }
}