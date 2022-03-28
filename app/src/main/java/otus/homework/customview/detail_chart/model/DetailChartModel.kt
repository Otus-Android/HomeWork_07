package otus.homework.customview.detail_chart.model

import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF

data class DetailChartModel(
    val chartPoints: List<Point> = emptyList(),
    val chartPath: Path = Path(),
    val horizontalLines: List<LineText> = emptyList(),
    val verticalLines: List<LineText> = emptyList(),
    val mainRectF: RectF = RectF(),
    val chartTextPadding: Int = 0,
    val boundsHorizontal: Rect = Rect(),
    val boundsVertical: Rect = Rect(),
    val maxRoundedAmount: Int = 0
)