package otus.homework.customview

import android.graphics.Color
import android.graphics.Paint


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
        return Paint().apply {
                color = Color.parseColor(colors[sliceCount])
            isAntiAlias = true
        }
    }
}