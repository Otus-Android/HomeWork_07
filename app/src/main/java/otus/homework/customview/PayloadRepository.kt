package otus.homework.customview

import android.content.Context
import com.fasterxml.jackson.databind.ObjectMapper
import otus.homework.customview.model.PayloadModel

class PayloadRepository(private val context: Context) {
    fun getData(): Array<PayloadModel> {
        val payloadsJson = context.resources.openRawResource(R.raw.payload)
            .bufferedReader()
            .use { it.readText() }
        return ObjectMapper().readValue(payloadsJson, Array<PayloadModel>::class.java)
    }
}