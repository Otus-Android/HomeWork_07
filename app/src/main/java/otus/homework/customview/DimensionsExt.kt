package otus.homework.customview

import android.content.res.Resources

val Int.dp: Float
    get() = (this * Resources.getSystem().displayMetrics.density)

/*val Int.sp: Float
    get() = (this * Resources.getSystem().displayMetrics.scaledDensity)*/

val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()
