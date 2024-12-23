package otus.homework.customview

import android.graphics.Color

class Palette {
    private val colors = arrayOf(
        "#BBAACC", "#CCBBAA", "#AACCBB", "#AABBCC", "#BBCCAA", "#CCAABB", "#90B0D0", "#B090D0", "#90D0B0", "#D090B0"
    )
    private val catColor : MutableMap<String, Int> = mutableMapOf()

    fun mapColors(cats : List<String>) {
        var index = 0
        for (cat in cats) {
            catColor[cat] = Color.parseColor(colors[index++ % colors.size])
        }
    }

    fun getColor(cat : String) : Int {
        var color : Int? = catColor[cat]
        if (color == null) {
            color = Color.parseColor(colors[catColor.size % colors.size])
            catColor[cat] = color
        }
        return color
    }
}