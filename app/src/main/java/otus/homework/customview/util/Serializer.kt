package otus.homework.customview.util

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import otus.homework.customview.R
import otus.homework.customview.pojo.Expense
import otus.homework.customview.sealed.Result
import java.io.InputStreamReader
import java.lang.reflect.Type

object Serializer {
    private val gson = Gson()
    private val listChartDataType: Type = object : TypeToken<List<Expense>?>() {}.type

    fun deserialize(context: Context): Result {
        return try {
            val expenses: List<Expense> = gson.fromJson(
                JsonReader(InputStreamReader(context.resources.openRawResource(R.raw.payload))),
                listChartDataType)
            Result.Expenses(expenses)

        } catch (e: Exception) {
            Log.d("ViewModel", "$e")
            Result.Error(e)
        }
    }
}