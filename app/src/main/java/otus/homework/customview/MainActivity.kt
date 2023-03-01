package otus.homework.customview

import android.graphics.Color.parseColor
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.os.PersistableBundle
import android.view.*
import org.json.JSONArray
import otus.homework.customview.data.Category
import otus.homework.customview.data.Segment
import otus.homework.customview.databinding.ActivityMainBinding
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    var orientationMode = 0
    private val colorsList by lazy {
        listOf(
            parseColor("#041763"),
            parseColor("#66F53B"),
            parseColor("#FC0505"),
            parseColor("#884EA0"),
            parseColor("#EAF2F8"),
            parseColor("#A9CCE3"),
            parseColor("#3498DB"),
            parseColor("#76D7C4"),
            parseColor("#FCFF61"),
            parseColor("#D4AC0D"),
            parseColor("#DC7633"),
            parseColor("#EB0CB7"),
        )
    }
    private var segmentList = ArrayList<Segment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fillDataList()

        orientationMode = resources.configuration.orientation

        binding.pieChart.setData(segmentList)
        binding.linearChart.setData(segmentList, orientationMode)
    }

    private fun fillDataList() {
        val categoriesList = ArrayList<Category>()
        readFromJson(categoriesList)

        categoriesList.forEachIndexed { index, category ->
            val segment = Segment(category.name, colorsList[index], category.amount, category.time)
            segmentList.add(segment)
        }
    }

    private fun readFromJson(categoriesList: ArrayList<Category>) {
        try {
            val inputStream = resources.openRawResource(R.raw.payload)
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)

            for(i in 0 until jsonArray.length()) {
                val jsonData = jsonArray.getJSONObject(i)
                val category = Category(
                    jsonData.getInt("id"),
                    jsonData.getString("name"),
                    jsonData.getInt("amount").toFloat(),
                    jsonData.getString("category"),
                    jsonData.getInt("time")
                )
                categoriesList.add(category)
            }
            inputStream.close()
        } catch (io: IOException) {
            io.printStackTrace()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.chart_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.item_pie -> {
                binding.pieChart.visibility = View.VISIBLE
                binding.linearChart.visibility = View.GONE
                true
            }
            R.id.item_linear -> {
                binding.linearChart.visibility = View.VISIBLE
                binding.pieChart.visibility = View.GONE
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }

}