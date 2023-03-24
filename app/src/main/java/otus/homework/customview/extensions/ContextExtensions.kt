package otus.homework.customview.extensions

import android.content.Context
import androidx.annotation.RawRes
import com.google.gson.Gson
import otus.homework.customview.model.Payload
import java.nio.charset.Charset

fun Context.getPayloadsFromJson(
    @RawRes rawResource: Int
): List<Payload> {
    return try {
        val jsonString = resources.openRawResource(rawResource).use {
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