package otus.homework.customview


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.InputStream

private const val TAG = "debug"


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val inputStream: InputStream = this.assets.open("payload.json")
        val bytesArray = ByteArray(inputStream.available())
        inputStream.read(bytesArray)
        val jsonFile = String(bytesArray)

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val listType = Types.newParameterizedType(List::class.java, Pay::class.java)
        val jsonAdapter: JsonAdapter<List<Pay>> = moshi.adapter(listType)
        val pays: List<Pay>? = jsonAdapter.fromJson(jsonFile)



        if (pays != null) {
            val amountFromCategory = getAmountFromCategory(pays)
            //Log.d(TAG, "${amountFromCategory.values.sum()}")
            val allAmount = amountFromCategory.values.sum()
            val amountFromCategoryArc = mutableMapOf<String, Float>()

            amountFromCategory.forEach{
                amountFromCategoryArc[it.key] = it.value.toFloat()/(allAmount/360)
            }
            Log.d(TAG, "${amountFromCategoryArc.values}")
            findViewById<CustomViewPieChart>(R.id.customViewPieChart).setValue(amountFromCategoryArc)
        }
    }
}

private fun getAmountFromCategory(pays: List<Pay>): Map<String, Int> {

    val category = mutableSetOf<String>()

    val resultMap = mutableMapOf<String, Int>()

    pays.forEach {
        category.add(it.category)
    }

    val size = category.size
    var n = 0

    while (n < size) {
        var sum = 0
        val itemCategory = category.elementAt(n)
        pays.forEach {
            if (it.category == itemCategory) {
                sum += it.amount
            }
            resultMap.put(itemCategory, sum)
        }
        n++
    }
    return resultMap
}
