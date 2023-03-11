package otus.homework.customview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import otus.homework.customview.custom_views.PieChart
import otus.homework.customview.model.Payload
import java.nio.charset.Charset

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val payloads = getPayloadsFromJson()

        findViewById<PieChart>(R.id.pie_chart).setPayloadsData(payloads)
    }

    private fun getPayloadsFromJson(): List<Payload> {
        return try {
            val jsonString = resources.openRawResource(R.raw.payload).use {
                val buffer = ByteArray(it.available())
                it.read(buffer)
                it.close()
                String(buffer, Charset.forName("UTF-8"))
            }
            Gson().fromJson(jsonString, Array<Payload>::class.java).asList()
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }
}