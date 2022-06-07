package otus.homework.customview

import android.graphics.Paint
import android.graphics.Path
import androidx.annotation.ColorInt

data class AxisChartCategoryItem(
    val category: String,
    @ColorInt
    val color: Int,
    val path: Path,
    val textAlign: Paint.Align
)