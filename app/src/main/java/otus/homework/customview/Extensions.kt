package otus.homework.customview

import android.content.Context
import android.view.MotionEvent
import androidx.annotation.RawRes
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.BufferedReader

inline fun <reified T> Context.decodeFromJsonFileResId(@RawRes resId: Int): T {
    val jsonString = resources.openRawResource(resId)
        .bufferedReader()
        .use(BufferedReader::readText)

    return Json.decodeFromString(jsonString)
}

fun Float.ceil(): Int = kotlin.math.ceil(this).toInt()