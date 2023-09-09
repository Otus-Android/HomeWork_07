package otus.homework.customview

import android.content.res.Resources.getSystem


val Int.dp: Int get() = (this * getSystem().displayMetrics.density).toInt()
val Int.sp: Int get() = (this * getSystem().displayMetrics.scaledDensity).toInt()
