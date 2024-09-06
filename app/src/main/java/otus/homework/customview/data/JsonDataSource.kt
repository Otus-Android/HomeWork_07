package otus.homework.customview.data

import android.content.res.Resources
import com.google.gson.Gson
import java.io.InputStreamReader
import javax.inject.Inject

class JsonDataSource @Inject constructor(private val resources: Resources) {

    fun loadPayloads(payloadId: Int): List<Payload> {
        readJsonFile(payloadId).let {
            return parseJson(it)
        }
    }

    private fun readJsonFile(resId: Int): String {
        val inputStream = resources.openRawResource(resId)
        val reader = InputStreamReader(inputStream)
        return reader.readText().also {
            reader.close()
        }
    }

    private fun parseJson(jsonString: String): List<Payload> {
        val gson = Gson()
        return gson.fromJson(jsonString, Array<Payload>::class.java).toList()
    }

}