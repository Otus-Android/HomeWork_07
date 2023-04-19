package otus.homework.customview


import android.graphics.Color
import androidx.annotation.ColorInt
import java.util.*

object ColorGenerator {

    @ColorInt
    fun generateColor(): Int {
        val rnd = Random()
        return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
    }

    fun generatePalette(
        size: Int,
        baseColor: Int,
        adjacentColors: Boolean
    ): IntArray {
        val colors = IntArray(size)
        colors[0] = baseColor

        val hsv = FloatArray(3)
        Color.RGBToHSV(
            Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor),
            hsv
        )

        val step = 240.0 / size
        val baseHue = hsv[0]

        for (i in 1 until size) {
            val nextColorHue = (baseHue + step * i) % 240.0
            colors[i] = Color.HSVToColor(floatArrayOf(nextColorHue.toFloat(), hsv[1], hsv[2]))
        }

        if (!adjacentColors && size > 2) {
            var i = 0
            var j = size / 2

            while (j < size) {
                colors[i] = colors[j].also { colors[j] = colors[i] }

                i += 2
                j += 2
            }
        }

        return colors
    }
}