package otus.homework.customview

import android.util.TypedValue
import android.view.View

fun View.dpToPx(dp: Float): Int {
    val r = context.resources

    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp,
        r.displayMetrics
    ).toInt()
}