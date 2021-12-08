package otus.homework.customview.state

import android.graphics.PointF
import java.io.Serializable

class GraphState : Serializable {
    val path: MutableList<PointF> = mutableListOf()
    val linesY: MutableList<Float> = mutableListOf()

    fun addToPath(x: Float, y: Float) {
        path.add(PointF(x, y))
    }

    fun addToLinesY(y: Float) {
        linesY.add(y)
    }

    fun clear() {
        path.clear()
        linesY.clear()
    }
}