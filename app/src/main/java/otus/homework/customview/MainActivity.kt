package otus.homework.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import otus.homework.customview.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // get dto from json
        val stringJson = resources.openRawResource(R.raw.payload)
            .bufferedReader()
            .use { it.readText() }
        val dto = Gson().fromJson(stringJson, Array<PurchaseDto>::class.java)

        // create dataset from dto
        val data = mutableMapOf<String, Int>()
        dto.forEach {
            if(data.containsKey(it.category)) {
                val currentAmount = data[it.category]!!
                data[it.category] = currentAmount + it.amount
            } else {
                data[it.category] = it.amount
            }
        }

        // we set data on activity first start only
        // the custom view will save it's state on config change
        if (savedInstanceState == null) {
            binding.pieChartView.updateData(data)
        }

        binding.pieChartView.setOnCategoryClickListener {
            Toast.makeText(this, it, Toast.LENGTH_LONG).show()
        }

    }
}


fun String.log() {
    Log.d("myLog", this)
}