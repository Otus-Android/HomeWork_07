package otus.homework.customview

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types.newParameterizedType
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import otus.homework.customview.data.Purchase
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
    val type = newParameterizedType(MutableList::class.java, Purchase::class.java)
    return Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build().adapter<List<Purchase>>(type).fromJson(payloadString)
}