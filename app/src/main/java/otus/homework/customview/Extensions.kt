package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.text.StaticLayout
import android.util.TypedValue
import androidx.core.graphics.withTranslation

fun Context.dpToPx(dp: Int): Float {
    return dp.toFloat() * this.resources.displayMetrics.density
}

fun Context.spToPx(sp: Int): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        sp.toFloat(),
        this.resources.displayMetrics
    )
}

fun StaticLayout.draw(canvas: Canvas, x: Float, y: Float) {
    canvas.withTranslation(x, y) {
        draw(this)
    }
}