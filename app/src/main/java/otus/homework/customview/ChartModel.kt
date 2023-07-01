package otus.homework.customview

import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import otus.homework.customview.MyApp.Companion.myResource
import java.nio.charset.Charset

class ChartModel : ViewModel() {

    var pieData: Map<String, Int> = HashMap()
    var beginArc = ArrayList<Float>()
    var lengthArc = ArrayList<Float>()
    var scaleArc: List<Float>
    var checkedIndex: Int = -1
    private val sumData: Int
    private val myData: List<PayLoad>
    private var startAngle = 0f
    private var sweepAngle = 0f

    init {
        myData = loadData()
        pieData = grafData()
        sumData = pieData.map { it -> it.value }.sum()
        pieData.forEach { _, i ->
            beginArc.add(startAngle)
            sweepAngle = (i * 360f) / sumData
            lengthArc.add(sweepAngle)
            System.out.println("startAngle $startAngle")
            startAngle += sweepAngle
        }
        scaleArc = pieData.map { _ -> 1.0f }
    }

    fun grafData() = myData.groupingBy { it.category }
        .fold(0) { summ, category -> summ + category.amount }

    fun loadData(): List<PayLoad> {
        val gson = Gson()
        val type = object : TypeToken<List<PayLoad>>() {}.type
        myResource.reset()
        val myJson = myResource.bufferedReader(Charset.defaultCharset())
        return gson.fromJson(myJson, type)
    }

    fun setScale(sector: Int) {
        scaleArc = scaleArc.mapIndexed { index, scale ->
            if (index == sector)
                1.05f
            else
                1.0f
        }
    }

    fun setChecked(index: Int) {
        checkedIndex = index
    }
}
