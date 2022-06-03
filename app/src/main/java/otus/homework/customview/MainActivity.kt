package otus.homework.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val jsonFileString = getJsonData()
        val listPersonType = object : TypeToken<List<PayLoad>>() {}.type
        var payload: List<PayLoad> = Gson().fromJson(jsonFileString, listPersonType)
        var categoryMap: MutableMap<String,Int> = mutableMapOf()
        var summary = 0
        payload.forEach {
            if(categoryMap.containsKey(it.category)){
                var amount = categoryMap[it.category]?.plus(it.amount)
                    categoryMap[it.category] = amount!!
                summary += it.amount
            } else{
                categoryMap[it.category] = it.amount
                summary += it.amount
            }
        }
        Log.d("Category", categoryMap.toString() + "Общая сумма денег $summary")

        val pieChartView = findViewById<PieChartView>(R.id.pieChartView)
        val graphView = findViewById<GraphView>(R.id.graphView)
        graphView.setValues(payload)
        pieChartView.setValues(categoryMap,summary)
        pieChartView.setOnSectorListener  =
            { category ->
                Toast.makeText(this,"Категория $category", Toast.LENGTH_SHORT).show()
                val payloadList = payload.filter { it.category == category }
                graphView.setValues(payloadList)
            }



    }





    fun getJsonData(): String? {
        val jsonString: String
        try {
            jsonString = resources.openRawResource(R.raw.payload).bufferedReader().use { it.readText() }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            return null
        }
        return jsonString
    }
}





