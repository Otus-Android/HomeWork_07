package otus.homework.customview.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import otus.homework.customview.R
import otus.homework.customview.dto.CategoryDTO

class JsonMapper(private val context: Context) {

    fun mapJson(): List<CategoryDTO> {
        val json = context.resources
            .openRawResource(R.raw.payload)
            .bufferedReader()
            .use { it.readText() }

        val type = object : TypeToken<List<CategoryDTO>>() {}.type
        return Gson().fromJson(json, type)
    }
}