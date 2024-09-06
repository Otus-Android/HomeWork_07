package otus.homework.customview.utils

import android.content.res.Resources
import android.util.TypedValue

val Int.dp: Int
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), Resources.getSystem().displayMetrics
    ).toInt()

val Int.px: Int
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_PX, this.toFloat(), Resources.getSystem().displayMetrics
    ).toInt()

val Int.sp: Int
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP, this.toFloat(), Resources.getSystem().displayMetrics
    ).toInt()