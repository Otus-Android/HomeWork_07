package otus.homework.customview

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.BufferedReader
import java.io.InputStreamReader

fun getPayload(context: Context): List<Purchase>? {
    val bufferedReader = BufferedReader(
        InputStreamReader(
            context.resources.openRawResource(
                context.resources.getIdentifier(
                    "payload",
                    "raw", context.packageName
                )
            )
        )
    )
    val payloadString = bufferedReader.use { it.readText() }
    return payloadString.fromJson()
}

inline fun <reified T: Any> String.fromJson() : T? {
    return Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build().adapter(T::class.java).fromJson(this)
}