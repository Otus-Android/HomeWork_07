package otus.homework.customview.utils

import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import otus.homework.customview.domain.Category
import otus.homework.customview.domain.Spend
import otus.homework.customview.presentation.pieChart.model.PieData
import otus.homework.customview.presentation.pieChart.model.PieSlice
import otus.homework.customview.presentation.timeLine.model.SpendModel
import otus.homework.customview.presentation.timeLine.model.TimeLineData
import java.util.Calendar

val mockPieData = PieData(
    totalValue = 100,
    pieSlices = mapOf(
        Category.SPORTS.title to PieSlice(
            categoryTitle = Category.SPORTS.title,
            amount = 30,
            startAngle = 0f,
            sweepAngle = 100f,
            indicatorCircleLocation = PointF(),
            thickness = 0.4f,
            paint = Paint().apply { color = Color.RED }
        ),
        Category.ALCOHOL.title to PieSlice(
            categoryTitle = Category.ALCOHOL.title,
            amount = 25,
            startAngle = 100f,
            sweepAngle = 80f,
            indicatorCircleLocation = PointF(),
            thickness = 0.15f,
            paint = Paint().apply { color = Color.GRAY }
        ),
        Category.TRANSPORT.title to PieSlice(
            categoryTitle = Category.TRANSPORT.title,
            amount = 25,
            startAngle = 180f,
            sweepAngle = 90f,
            indicatorCircleLocation = PointF(),
            thickness = 0.25f,
            paint = Paint().apply { color = Color.BLUE }
        ),
        Category.GROCERIES.title to PieSlice(
            categoryTitle = Category.GROCERIES.title,
            amount = 25,
            startAngle = 270f,
            sweepAngle = 90f,
            indicatorCircleLocation = PointF(),
            thickness = 0.25f,
            paint = Paint().apply { color = Color.GREEN }
        ),
    )
)

val mockTimeLineData = TimeLineData(
    selected = Category.SPORTS, available = Category.entries.toSet(), spends = List(10) {
        SpendModel(
            name = "Sport $it",
            amount = 100 * it,
            category = Category.SPORTS,
            date = (Calendar.getInstance().timeInMillis + it * 3_600 * 24).toString()
        )
    },
    totalDays = 10,
    maxAmount = 100 * 10
)