package otus.homework.customview

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

fun readRawFileAsString(context: Context, resourceId: Int): String {
    val inputStream = context.resources.openRawResource(resourceId)
    val reader = BufferedReader(InputStreamReader(inputStream))
    return reader.readText()
}