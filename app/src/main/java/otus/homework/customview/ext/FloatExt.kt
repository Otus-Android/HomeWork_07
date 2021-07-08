package otus.homework.customview.ext

import android.content.res.Resources

fun Float.dPToPx() = (this * Resources.getSystem().displayMetrics.density)

fun Float.pxToDp() = (this / Resources.getSystem().displayMetrics.density)