package otus.homework.customview.models

import android.graphics.Color
import android.graphics.Paint
import kotlin.random.Random

data class PiePiece(
    val category: String,
    val start: Float,
    val end: Float,
    val paint: Paint,
    val data: List<Metka>
)

fun createPaint(): Paint {
    val paint = Paint()
    paint.color = Color.argb(
        255, Random.nextInt(255),Random.nextInt(255), Random.nextInt(255)
    )
    paint.isAntiAlias = true
    paint.style = Paint.Style.FILL_AND_STROKE
    paint.strokeCap = Paint.Cap.BUTT
    return paint
}