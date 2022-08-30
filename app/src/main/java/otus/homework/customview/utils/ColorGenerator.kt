package otus.homework.customview.utils

import android.graphics.Color
import androidx.annotation.ColorInt
import java.util.*

object ColorGenerator {

    @ColorInt
    fun generateColor(): Int {
        val rnd = Random()
        return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
    }
}