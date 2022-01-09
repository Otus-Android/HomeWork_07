package otus.homework.customview.linegraph

import android.graphics.PointF
import java.io.Serializable

class LineGraphData : Serializable {

    val path: MutableList<PointF> = mutableListOf()
    val linesY: MutableList<Float> = mutableListOf()

    fun clear() {
        path.clear()
        linesY.clear()
    }

    fun addToLinesY(y: Float) {
        linesY.add(y)
    }

    fun addToPath(x: Float, y: Float) {
        path.add(PointF(x, y))
    }


}