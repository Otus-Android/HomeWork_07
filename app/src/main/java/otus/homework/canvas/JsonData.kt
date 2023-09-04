import android.content.Context
import android.graphics.Color
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import otus.homework.canvas.PieChartSectorData
import otus.homework.canvas.R

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
        val jonDataArray = getJsonData() ?: return null
        val pieChartSectorData = mutableListOf<PieChartSectorData>()
        for (data in jonDataArray) {
            pieChartSectorData.add(
                PieChartSectorData(
                    angle = data.amount.toFloat(),
                    radius = data.time.toFloat(),
                    text = data.name,
                    category = data.category,
                )
            )
        }
        return pieChartSectorData
    }
}