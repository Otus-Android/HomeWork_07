package otus.homework.customview

import android.graphics.Paint
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
import otus.homework.customview.models.Point
import otus.homework.customview.models.createPaint
import java.io.Reader
import java.lang.reflect.Type


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val pieChartView = findViewById<PieChartView>(R.id.pieChartView)
        val graphView = findViewById<GraphView>(R.id.graphView)

        val data = loadJSONFromAsset()
        var pie = mapToPie(data)
        var points = mapToPoints(data!!)
        Log.d("DDDDD", points.toString())
        pieChartView.setData(pie)
        graphView.setData(points)

        val pieChartClickListener = object : PieChartClickListener {
            override fun onClick(category: String) {
                pie = pie?.map {
                    if (it.category == category) it.copy(isClicked = true)
                    else it.copy(isClicked = false)
                }
                pieChartView.setData(pie)
            }
        }

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
            finishPoint = countElem / count * 360f
            PiePiece(
                category = map.key,
                start = startPoint,
                end = finishPoint,
                paint = createPaint(),
                data = map.value,
            )
        }
    }

    private fun mapToPoints(data: List<Metka>): Map<Pair<String, Paint>, Map<Int, Point>> {
        val minAmount = data.minOf { it.amount }.toFloat()
        val maxAmount = ((data.maxOf { it.amount }) - minAmount).toFloat()
        val minTime = data.minOf { it.time }.toFloat()
        val maxTime = ((data.maxOf { it.time }) - minTime).toFloat()
        val group = data.groupBy { it.category }

        val outMap: MutableMap<Pair<String, Paint>, Map<Int, Point>> = mutableMapOf()
        group.forEach { map ->
            var i = 0

            val pointMap = mutableMapOf<Int, Point>()

            map.value.forEach {
                pointMap[++i] =
                    Point(
                        coorX = ((it.time - minTime) / maxTime) * 600f,
                        coorY = ((it.amount - minAmount) / maxAmount) * 400f,
                    )
            }
            outMap[Pair(map.key, createPaint())] = pointMap
        }

        return outMap
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