import android.content.Context
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import otus.homework.canvas.LineGraphData
import otus.homework.canvas.PieChartSectorData
import otus.homework.canvas.R
import java.text.SimpleDateFormat

class JsonData(private val context: Context) {

    data class JsonData(
        val id: Int,
        val name: String,
        val amount: Int,
        val category: String,
        val time: Int
    )

    fun getJsonData(): List<JsonData>? {
        try {
            val jsonFile = context.resources.openRawResource(R.raw.payload).bufferedReader()
            val arrayTutorialType = object : TypeToken<List<JsonData>>() {}.type
            val purchases = Gson().fromJson<List<JsonData>>(jsonFile, arrayTutorialType)
            return purchases
        } catch (error: Exception) {
            return null
        }
    }

    fun getPieChartSectorData(): List<PieChartSectorData>? {
        val jsonDataArray = getJsonData() ?: return null
        val pieChartSectorDataMap = mutableMapOf<String, PieChartSectorData>()
        for (data in jsonDataArray) {
            if (pieChartSectorDataMap.containsKey(data.category)) {
                pieChartSectorDataMap[data.category]?.angle = pieChartSectorDataMap[data.category]?.angle!! + data.amount.toFloat()
            }
            else {
                pieChartSectorDataMap += data.category to
                        PieChartSectorData(
                            angle = data.amount.toFloat(),
                            text = data.category,
                            category = data.category
                        )
            }
        }
        val pieChartSectorData = mutableListOf<PieChartSectorData>()
        for ((key, value) in pieChartSectorDataMap) {
            pieChartSectorData += value
        }
        return pieChartSectorData
    }


    //private fun timeToString_(time: Int) = "$time" // TODO
    private fun timeToString(time: Float) = SimpleDateFormat.getDateTimeInstance().format(time.toInt())

    fun getLineGraphData(): MutableMap<String, List<LineGraphData>>? {
        val jsonDataArray = getJsonData() ?: return null
        var lineGraphData = mutableMapOf<String, MutableList<LineGraphData>>()
        for (data in jsonDataArray) {
            val addData = LineGraphData(data.time.toFloat(), data.amount.toFloat(), ::timeToString)
            if (lineGraphData.containsKey(data.category)) {
                lineGraphData[data.category]!!.add(addData)
            } else {
                lineGraphData += data.category to mutableListOf(addData)
            }
        }
        return lineGraphData as MutableMap<String, List<LineGraphData>>
    }
}
