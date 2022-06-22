package otus.homework.customview.pieChart

import androidx.annotation.ColorInt
import java.io.Serializable

data class PieChartState(
    val colorStates: List<ColorState>,
): Serializable {

    private val totalValue = colorStates.sumOf { it.value }

    fun getPart(value: Int): Float {
        assert(value > 0 || value <= totalValue)
        return value.toFloat() / totalValue
    }

    data class ColorState(
        val id: String,
        val value: Int,
        @ColorInt val color: Long
    ): Serializable

    companion object {
        fun default() = PieChartState(
            colorStates = emptyList()
        )
    }

}