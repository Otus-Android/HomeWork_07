package otus.homework.customview

import android.content.res.Resources
import android.util.Log

val Int.dp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()

val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

val Any.TAG: String
    get() = javaClass.simpleName
