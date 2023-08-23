package otus.homework.customview

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class FileUtils {

    object AssetsLoader {

        fun loadTextFromAsset(context: Context, file: String): String {
            return context.assets.open(file).bufferedReader().use { reader ->
                reader.readText()
            }
        }

        fun getDataFromText(text: String): List<PayLoadModel> {
            val listPayLoadModelType = object : TypeToken<List<PayLoadModel>>() {}.type
            val payloads: List<PayLoadModel> = Gson().fromJson(text, listPayLoadModelType)
            return payloads
        }

    }
}