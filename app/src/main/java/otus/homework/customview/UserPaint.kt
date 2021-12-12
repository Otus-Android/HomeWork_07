package otus.homework.customview

import android.graphics.Color
import android.graphics.Paint

class UserPaint {

    val blackPaint: Paint =
        Paint().apply {
            color = Color.BLACK
            strokeWidth = 3f
            flags = Paint.ANTI_ALIAS_FLAG
            style = Paint.Style.FILL
            textSize = 60f
            textAlign = Paint.Align.CENTER

        }

    val redStrokePaint: Paint = Paint().apply {
        color = Color.RED
    }
    val blueStrokePaint: Paint = Paint().apply {
        color = Color.BLUE
    }
    val grayStrokePaint: Paint = Paint().apply {
        color = Color.GRAY
    }
    val magentaStrokePaint: Paint = Paint().apply {
        color = Color.MAGENTA
    }
    val greenStrokePaint: Paint = Paint().apply {
        color = Color.GREEN
    }
    val cyanStrokePaint: Paint = Paint().apply {
        color = Color.CYAN
    }
    val yellowStrokePaint: Paint = Paint().apply {
        color = Color.YELLOW
    }
    val dkgrayStrokePaint: Paint = Paint().apply {
        color = Color.DKGRAY
    }


    var color = mutableListOf(
        redStrokePaint, blueStrokePaint,
        grayStrokePaint, magentaStrokePaint, greenStrokePaint, cyanStrokePaint, yellowStrokePaint,
        dkgrayStrokePaint)
}









