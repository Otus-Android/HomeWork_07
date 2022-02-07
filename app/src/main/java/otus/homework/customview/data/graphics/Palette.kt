package otus.homework.customview.data.graphics

import android.graphics.Color
import android.graphics.CornerPathEffect
import android.graphics.Paint

object Palette {

  val textPaint: Paint = Paint().apply {
    color = Color.BLACK
    isAntiAlias = true
    textSize = 30f
    textAlign = Paint.Align.CENTER
  }

  val stroke: Paint = Paint().apply {
    color = Color.BLACK
    strokeWidth = 2f
    isAntiAlias = true
    style = Paint.Style.STROKE
    pathEffect = CornerPathEffect(2f)
  }

  val colors = listOf(
    "#47d147",
    "#ff3333",
    "#1a1aff",
    "#ffff1a",
    "#33ffff",
    "#ffa31a",
    "#d9d9d9",
    "#4d4dff",
    "#99994d",
    "#996633"
  )
}