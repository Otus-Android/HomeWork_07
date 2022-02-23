package otus.homework.customview.ui.utils

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.IntRange
import androidx.core.graphics.ColorUtils
import kotlin.random.Random
import kotlin.random.nextInt

class ColorUtils {

    companion object {
        @ColorInt
        fun randomColor(
            @IntRange(from = 0, to = 255) minLevel: Int = 0,
            @IntRange(from = 0, to = 255) maxLevel: Int = 255,
            saturationRange: ClosedFloatingPointRange<Float> = 0.4f..0.7f,
            lightnessRange: ClosedFloatingPointRange<Float> = 0.45f..0.65f,
        ): Int {
            var r = Random.nextInt(minLevel..maxLevel)
            var g = Random.nextInt(minLevel..maxLevel)
            var b = Random.nextInt(minLevel..maxLevel)
            var (_, s, l) = FloatArray(3).also { ColorUtils.RGBToHSL(r, g, b, it) }
            while (l !in lightnessRange || s !in saturationRange) {
                r = Random.nextInt(minLevel..maxLevel)
                g = Random.nextInt(minLevel..maxLevel)
                b = Random.nextInt(minLevel..maxLevel)
                FloatArray(3).also {
                    ColorUtils.RGBToHSL(r, g, b, it)
                    s = it[1]
                    l = it[2]
                }
            }
            return Color.rgb(r, g, b)
        }
    }

}