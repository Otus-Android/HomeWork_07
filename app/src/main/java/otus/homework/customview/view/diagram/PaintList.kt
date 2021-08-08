package otus.homework.customview.view.diagram

import android.content.Context
import android.graphics.Paint
import otus.homework.customview.R

class PaintList(private val context: Context) {

    private val _style = Paint.Style.STROKE
    private val paint1 = Paint().apply {
        color = context.getColor(R.color.Coral)
        style = _style
    }
    private val paint2 = Paint().apply {
        color = context.getColor(R.color.LightGreen)
        style = _style

    }
    private val paint3 = Paint().apply {
        color = context.getColor(R.color.MediumSlateBlue)
        style = _style
    }
    private val paint4 = Paint().apply {
        color = context.getColor(R.color.LightPink)
        style = _style
    }
    private val paint5 = Paint().apply {
        color = context.getColor(R.color.Moccasin)
        style = _style
    }
    private val paint6 = Paint().apply {
        color = context.getColor(R.color.Peru)
        style = _style
    }
    private val paint7 = Paint().apply {
        color = context.getColor(R.color.SkyBlue)
        style = _style
    }
    private val paint8 = Paint().apply {
        color = context.getColor(R.color.Olive)
        style = _style
    }
    private val paint9 = Paint().apply {
        color = context.getColor(R.color.SlateGray)
        style = _style
    }
    private val paint10 = Paint().apply {
        color = context.getColor(R.color.Violet)
        style = _style
    }

    fun painList(): List<Paint> =
        listOf(paint1, paint2, paint3, paint4, paint5, paint6, paint7, paint8, paint9, paint10)
}