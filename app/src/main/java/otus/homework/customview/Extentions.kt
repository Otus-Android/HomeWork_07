package otus.homework.customview

import android.content.Context

fun Int.dp(context: Context) = context.resources.displayMetrics.density * this
