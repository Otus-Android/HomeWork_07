package otus.homework.customview.common

import android.graphics.Color
import java.util.Random

object ColorUtils {

    fun getRandomColor(): Int {
        val rnd = Random()
        return Color.argb(
            255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256)
        )
    }
}