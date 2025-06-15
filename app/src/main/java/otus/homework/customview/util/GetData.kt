package otus.homework.customview.util

import android.content.Context
import android.util.Log
import com.google.gson.GsonBuilder
import otus.homework.customview.task_1.model.Category
import otus.homework.customview.task_1.model.Data

fun getData(context: Context, resourceId: Int): List<Data> {
    val list = mutableListOf<Data>()
    try {
        val inputStream = context.resources.openRawResource(resourceId)
        val jsonString = inputStream.bufferedReader().use { it.readText() }
        val gson = GsonBuilder()
            .registerTypeAdapter(Category::class.java, CategoryDeserializer())
            .create()
        val temp = gson.fromJson(jsonString, Array<Data>::class.java)
        list.addAll(temp)
    } catch (e: Exception) {
        Log.e("GetData", e.toString())
    }
    return list
}
