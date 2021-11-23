package otus.homework.customview.paychart

import android.graphics.Color
import android.graphics.Paint
import androidx.core.graphics.toColorInt


private const val ORANGE = "#FF5722"
object PaintStore {
    private val paintList = listOf(
        ORANGE.toColorInt(),
        Color.BLACK,
        Color.BLUE,
        Color.CYAN,
        Color.DKGRAY,
        Color.GREEN,
        Color.LTGRAY,
        Color.MAGENTA,
        Color.RED,
        Color.YELLOW,
    ).map {
        Paint().apply {
            color = it
        }
    }

    private var iterator: Iterator<Paint>? = null

    fun getPaint(): Paint {
        if (iterator == null) {
            iterator = paintList.iterator()
        }
        return if (iterator!!.hasNext()) {
            iterator!!.next()
        } else {
            iterator = paintList.iterator()
            iterator!!.next()
        }
    }

    fun reset() {
        iterator = paintList.iterator()
    }
}