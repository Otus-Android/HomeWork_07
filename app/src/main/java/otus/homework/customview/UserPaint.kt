package otus.homework.customview

import android.graphics.Color
import android.graphics.Paint
import androidx.core.graphics.rotationMatrix

class UserPaint {

    val blackPaint: Paint =
        Paint().apply {
            color = Color.BLACK
            strokeWidth = 3f
            flags = Paint.ANTI_ALIAS_FLAG
            style = Paint.Style.FILL
            textSize = 50f
            textAlign = Paint.Align.CENTER

        }

    val blackPaintStroke: Paint =
        Paint().apply {
            color = Color.BLACK
            strokeWidth = 3f
            flags = Paint.ANTI_ALIAS_FLAG
            style = Paint.Style.STROKE
        }


    val redPaint: Paint = Paint().apply {
        color = Color.RED
    }
    val bluePaint: Paint = Paint().apply {
        color = Color.BLUE
    }
    val grayPaint: Paint = Paint().apply {
        color = Color.GRAY
    }
    val magentaPaint: Paint = Paint().apply {
        color = Color.MAGENTA
    }
    val greenPaint: Paint = Paint().apply {
        color = Color.GREEN
    }
    val cyanPaint: Paint = Paint().apply {
        color = Color.CYAN
    }
    val yellowPaint: Paint = Paint().apply {
        color = Color.YELLOW
    }
    val dkgrayPaint: Paint = Paint().apply {
        color = Color.DKGRAY
    }


    var color = mutableListOf(
        redPaint, bluePaint,
        grayPaint, magentaPaint, greenPaint, cyanPaint, yellowPaint,
        dkgrayPaint)
}









