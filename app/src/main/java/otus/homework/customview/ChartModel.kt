package otus.homework.customview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import otus.homework.customview.MyApp.Companion.myResource
import java.nio.charset.Charset

class ChartModel() : ViewModel() {

    val myData = getData()
    val pieData:Map<String,Int> = grafData()

    fun grafData() = myData.groupingBy {it.category}
        .fold(0) { summ, category -> summ + category.amount }

    fun getData(): List<PayLoad>{
        val gson = Gson()
        val type = object : TypeToken<List<PayLoad>>() {}.type
        myResource.reset()
        val myJson = myResource.bufferedReader(Charset.defaultCharset())
        return  gson.fromJson<List<PayLoad>>(myJson, type)
    }
}
