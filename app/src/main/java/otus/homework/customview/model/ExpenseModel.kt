package otus.homework.customview.model

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import otus.homework.customview.R
import java.lang.Exception
import java.nio.file.Files
import java.nio.file.Paths

@SuppressLint("NewApi")
data class ExpenseModel(
    val id: Int,
    val name: String,
    val amount: Double,
    val category: String,
    val time: Long
) {

    companion object {
        fun readExpensesFromJson(
            context: Context
        ): List<ExpenseModel>? {
            return try {
                val gson = Gson()

                val reader = context.resources.openRawResource(R.raw.payload).bufferedReader()
                val models: List<ExpenseModel> =
                    gson.fromJson(reader, object : TypeToken<List<ExpenseModel>>() {}.type)
                reader.close()
                models
            } catch (e: Exception) {
                Log.e("EXPENSE MODEL", "readExpensesFromJson: failed to read from json file", e)
                emptyList()
            }
        }
    }
}
