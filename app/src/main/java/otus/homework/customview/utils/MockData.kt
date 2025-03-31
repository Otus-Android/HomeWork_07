package otus.homework.customview.utils

import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import otus.homework.customview.domain.Category
import otus.homework.customview.presentation.firstPieChart.model.PieData
import otus.homework.customview.presentation.firstPieChart.model.PieSlice

val mockData = PieData(
    totalValue = 100,
    pieSlices = mapOf(
        Category.SPORTS.title to PieSlice(
            categoryTitle = Category.SPORTS.title,
            amount = 25,
            startAngle = 0f,
            sweepAngle = 90f,
            indicatorCircleLocation = PointF(),
            paint = Paint().apply { color = Color.RED }
        ),
        Category.ALCOHOL.title to PieSlice(
            categoryTitle = Category.ALCOHOL.title,
            amount = 25,
            startAngle = 90f,
            sweepAngle = 90f,
            indicatorCircleLocation = PointF(),
            paint = Paint().apply { color = Color.GRAY }
        ),
        Category.TRANSPORT.title to PieSlice(
            categoryTitle = Category.TRANSPORT.title,
            amount = 25,
            startAngle = 180f,
            sweepAngle = 90f,
            indicatorCircleLocation = PointF(),
            paint = Paint().apply { color = Color.BLUE }
        ),
        Category.GROCERIES.title to PieSlice(
            categoryTitle = Category.GROCERIES.title,
            amount = 25,
            startAngle = 270f,
            sweepAngle = 90f,
            indicatorCircleLocation = PointF(),
            paint = Paint().apply { color = Color.GREEN }
        ),
    )
)