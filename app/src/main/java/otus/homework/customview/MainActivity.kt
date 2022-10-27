package otus.homework.customview

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import otus.homework.customview.customView.GraphView
import otus.homework.customview.customView.PieChartView
import otus.homework.customview.models.Metka
import otus.homework.customview.models.PiePiece
import otus.homework.customview.models.createPaint
import java.io.Reader
import java.lang.reflect.Type


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val pieChartView = findViewById<PieChartView>(R.id.pieChartView)
        val graphView = findViewById<GraphView>(R.id.graphView)
        val text = findViewById<TextView>(R.id.text)

        val pieChartClickListener = object : PieChartClickListener {
            override fun onClick(category: String) {
                text.text = category
            }
        }
        val data = loadJSONFromAsset()
        pieChartView.setData(
            mapToPie(data)
        )

        pieChartView.pieChartClickListener = pieChartClickListener
    }

    private fun mapToPie(data: List<Metka>?): List<PiePiece>? {

        val group = data?.groupBy { it.category }
        var startPoint = 0f
        var finishPoint = 0f
        var count = 0f
        data?.forEach {
            count += it.amount
        }
        return group?.map { map ->
            startPoint += finishPoint
            var countElem = 0f
            map.value.forEach {
                countElem += it.amount
            }
            finishPoint = countElem/count * 360f
            PiePiece(
                category = map.key,
                start = startPoint,
                end = finishPoint,
                paint = createPaint(),
                data = map.value,
            )
        }
    }

    private fun loadJSONFromAsset(): List<Metka>? =
        resources.openRawResource(R.raw.payload).bufferedReader().use {
            return getList(it, Metka::class.java)
        }

    private fun <T> getList(jsonArray: Reader?, clazz: Class<T>?): List<T>? {
        val typeOfT: Type = TypeToken.getParameterized(MutableList::class.java, clazz).type
        return Gson().fromJson(jsonArray, typeOfT)
    }
}