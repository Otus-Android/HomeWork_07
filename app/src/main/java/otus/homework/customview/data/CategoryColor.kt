package otus.homework.customview.data

import androidx.core.graphics.toColorInt

enum class CategoryColor(val colorCode: Int) {
    MAROON("#800000".toColorInt()),         // ~red
    SADDLE_BROWN("#804000".toColorInt()),   // ~red - yellow (orange)
    OLIVE("#808000".toColorInt()),          // ~yellow
    BILBAO("#408000".toColorInt()),         // ~yellow - green (chartreuse)
    GREEN("#008000".toColorInt()),          // green
    SALEM("#008040".toColorInt()),          // ~green-blue (cyan)
    TEAL("#008080".toColorInt()),           // ~light blue
    DARK_CERULEAN("#004080".toColorInt()),  // ~medium blue
    NAVY("#000080".toColorInt()),           // ~dark blue
    INDIGO("#400080".toColorInt()),         // dark blue - violet (indigo)
    PURPLE("#800080".toColorInt()),         // ~violet
    TYRIAN_PURPLE("#800040".toColorInt()),  // ~violet - red (magenta)
}
