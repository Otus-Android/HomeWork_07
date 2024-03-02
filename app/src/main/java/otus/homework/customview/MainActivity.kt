package otus.homework.customview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonObject
import otus.homework.customview.databinding.ActivityMainBinding
import otus.homework.customview.model.Store

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val jsonData = applicationContext.resources.openRawResource(
            applicationContext.resources.getIdentifier(
                "payload",
                "raw", applicationContext.packageName
            )
        ).bufferedReader().use { it.readText() }

        val jsonObj = JsonArray(Json.decodeFromString(jsonData))

        println("$jsonObj")

        val listStore = mutableListOf<Store>()
        val amounts = mutableListOf<Int>()

        for (i in 0 until jsonObj.size) {
            val id = jsonObj[i].jsonObject["id"]
            val name = jsonObj[i].jsonObject["name"]
            val amount = jsonObj[i].jsonObject["amount"]
            val category = jsonObj[i].jsonObject["category"]
            val time = jsonObj[i].jsonObject["time"]

            amounts.add(amount.toString().toInt())
            listStore.add(
                Store(
                    id = id.toString().toInt(),
                    name = name.toString(),
                    amount = amount.toString().toInt(),
                    category.toString(),
                    time.toString().toInt()
                )
            )
        }
        //println("$listStore")

        binding.chart.setValues(amounts)
        binding.chartRect.setValues(amounts)

    }
}