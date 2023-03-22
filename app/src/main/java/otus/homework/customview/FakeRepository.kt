package otus.homework.customview

import android.content.Context
import com.google.gson.Gson

/**
 * Represent a data source imitation.
 * Provide an array of [PurchaseDto] from JSON resource file.
 */
class FakeRepository(private val context: Context) {
    fun getData(): Array<PurchaseDto> {
        // get dto from json
        val stringJson = context.resources.openRawResource(R.raw.payload)
            .bufferedReader()
            .use { it.readText() }
        return Gson().fromJson(stringJson, Array<PurchaseDto>::class.java)
    }
}