package otus.homework.customview

import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

class UserPaint() {

val blackPaint: Paint
    get()=
    Paint().apply {
    color = Color.BLACK
    strokeWidth = 3f
    flags = Paint.ANTI_ALIAS_FLAG
    style = Paint.Style.FILL
        textSize= 60f
        textAlign = Paint.Align.CENTER
        //measureText()
        //breakText
    }

    val whitePaint: Paint = Paint().apply {
        color = Color.WHITE
        strokeWidth = 3f
        flags = Paint.ANTI_ALIAS_FLAG
        style = Paint.Style.FILL
    }

    val blackStrokePaint: Paint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 6f
        style = Paint.Style.STROKE
    }
    val redStrokePaint: Paint = Paint().apply {
        color = Color.RED
        strokeWidth = 3f
    }
    val blueStrokePaint: Paint = Paint().apply {
        color = Color.BLUE
        strokeWidth = 3f
    }
    val grayStrokePaint: Paint = Paint().apply {
        color = Color.GRAY
        strokeWidth = 3f
    }
    val magentaStrokePaint: Paint = Paint().apply {
        color = Color.MAGENTA
        strokeWidth = 3f
    }
    val greenStrokePaint: Paint = Paint().apply {
        color = Color.GREEN
        strokeWidth = 3f
    }
    val cyanStrokePaint: Paint = Paint().apply {
        color = Color.CYAN
        strokeWidth = 3f
    }
    val yellowStrokePaint: Paint = Paint().apply {
        color = Color.YELLOW
        strokeWidth = 3f
    }
    val dkgrayStrokePaint: Paint = Paint().apply {
        color = Color.DKGRAY
        strokeWidth = 3f
    }


    var color = mutableListOf<Paint>(redStrokePaint, blueStrokePaint,
        grayStrokePaint, magentaStrokePaint, greenStrokePaint, cyanStrokePaint, yellowStrokePaint,
        dkgrayStrokePaint)

}









