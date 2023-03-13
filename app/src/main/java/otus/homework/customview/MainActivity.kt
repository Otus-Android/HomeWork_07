package otus.homework.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.gson.Gson
import otus.homework.customview.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val stringJson = resources.openRawResource(R.raw.payload)
            .bufferedReader()
            .use { it.readText() }
        val dto = Gson().fromJson(stringJson, Array<PurchaseDto>::class.java)
        val data = mutableMapOf<String, Int>()
        dto.forEach {
            if(data.containsKey(it.category)) {
                val currentAmount = data[it.category]!!
                data[it.category] = currentAmount + it.amount
            } else {
                data[it.category] = it.amount
            }
        }
        if (savedInstanceState == null) {
            "update from activity".log()
            binding.pieChartView.updateData(data)
        }

    }
}


fun String.log() {
    Log.d("myLog", this)
}