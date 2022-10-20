package otus.homework.customview.tools

import android.graphics.Color
import androidx.annotation.ColorInt

class PieColors(private val numberOfColors: Int) {

    @ColorInt
    fun colors(): IntArray {
        val colors = IntArray(numberOfColors)
        for (i in colors.indices) {
            val hue = (i * (HUE_MAX - HUE_MIN) / numberOfColors)
            colors[i] = mapHueToColor(hue)
        }
        return colors
    }

    private fun mapHueToColor(hue: Float): Int = Color.HSVToColor(floatArrayOf(hue, 1F, 1F))

    companion object {
        private const val HUE_MIN = 0F
        private const val HUE_MAX = 360F
    }
}