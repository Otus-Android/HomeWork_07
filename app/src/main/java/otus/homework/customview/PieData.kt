package otus.homework.customview

import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import kotlin.collections.HashMap


class PieData(private val colors: Array<String>) {
    val pieSlices = HashMap<String, PieSlice>()
    var totalValue = 0f

    private var sliceCount = 0

    fun add(name: String, value: Int) {
        if (pieSlices.containsKey(name))
            pieSlices[name]?.let { it.value += value }
        else
            pieSlices[name] = PieSlice(name, value, createPaint())

        sliceCount += 1
        totalValue += value
    }

    private fun createPaint(): Paint {
        val index = sliceCount % colors.size

        return Paint().apply {
            color = Color.parseColor(colors[index])
            isAntiAlias = true
        }
    }
}