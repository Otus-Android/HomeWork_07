package otus.homework.customview

import android.content.res.Resources
import android.graphics.Color
import kotlin.random.Random


fun generateRandomColor(): Int =
    Color.argb(
        255,
        Random.nextInt(256),
        Random.nextInt(256),
        Random.nextInt(256)
    )

val Float.dp get() = this * Resources.getSystem().displayMetrics.density