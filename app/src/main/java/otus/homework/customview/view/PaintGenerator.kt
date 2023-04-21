package otus.homework.customview.view

import android.graphics.Paint
import androidx.core.content.ContextCompat
import otus.homework.customview.MainActivity
import otus.homework.customview.R

object PaintGenerator {

    private val colorList =
        listOf(
            ContextCompat.getColor(
                MainActivity.INSTANCE.applicationContext,
                R.color.pie_chart_color_0
            ),
            ContextCompat.getColor(
                MainActivity.INSTANCE.applicationContext,
                R.color.pie_chart_color_1
            ),
            ContextCompat.getColor(
                MainActivity.INSTANCE.applicationContext,
                R.color.pie_chart_color_2
            ),
            ContextCompat.getColor(
                MainActivity.INSTANCE.applicationContext,
                R.color.pie_chart_color_3
            ),
            ContextCompat.getColor(
                MainActivity.INSTANCE.applicationContext,
                R.color.pie_chart_color_4
            ),
            ContextCompat.getColor(
                MainActivity.INSTANCE.applicationContext,
                R.color.pie_chart_color_5
            ),
            ContextCompat.getColor(
                MainActivity.INSTANCE.applicationContext,
                R.color.pie_chart_color_6
            ),
            ContextCompat.getColor(
                MainActivity.INSTANCE.applicationContext,
                R.color.pie_chart_color_7
            ),
            ContextCompat.getColor(
                MainActivity.INSTANCE.applicationContext,
                R.color.pie_chart_color_8
            ),
            ContextCompat.getColor(
                MainActivity.INSTANCE.applicationContext,
                R.color.pie_chart_color_9
            )
        )

    fun getPaint(index: Int): Paint {
        return Paint().apply {
            isAntiAlias = true
            color = colorList[index % colorList.size]
            style = Paint.Style.STROKE
            strokeWidth = MainActivity.INSTANCE.applicationContext.resources
                .getDimensionPixelSize(R.dimen.pie_chart_stroke_width).toFloat()
        }
    }
}