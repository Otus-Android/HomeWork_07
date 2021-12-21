package otus.homework.customview

import android.view.View
import android.util.TypedValue

fun View.convertDpToPixels(dp: Float) =
    dp * this.resources.displayMetrics.density

fun View.convertSpToPixels(sp: Float) =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, this.resources.displayMetrics)