package otus.homework.customview

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import otus.homework.customview.models.PieSlice
import otus.homework.customview.models.Spend
import java.io.IOException
import java.io.InputStream
import java.io.Reader
import java.nio.file.Files
import java.nio.file.Paths

class MainActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val list = readJson()

        val chart = findViewById<PieChartView>(R.id.chart)
        chart.setItems(list)
    }

    private fun readJson(): List<Spend> {
        return try {
            var json : String? = null
            val inputStream: InputStream = resources.openRawResource(
                resources.getIdentifier("payload",
                    "raw", packageName
                ))

            json = inputStream.bufferedReader().use { it.readText() }
            val itemType = object : TypeToken<List<Spend>>() {}.type
            val gson = GsonBuilder().create()
            gson.fromJson<List<Spend>>(json, itemType)

        } catch (e: IOException) {
            emptyList()
        }
    }
}