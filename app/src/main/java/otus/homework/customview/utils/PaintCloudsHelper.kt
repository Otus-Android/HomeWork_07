package otus.homework.customview.utils

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View

class PaintCloudsHelper {

    private val p1 = Paint().apply { color = Color.parseColor("#d1dff6") }
    private val p2 = Paint().apply { color = Color.parseColor("#c2d6f6") }
    private val p3 = Paint().apply { color = Color.parseColor("#b2cbf2") }
    private val p4 = Paint().apply { color = Color.parseColor("#a0bff0") }

    fun paint(view: View, canvas: Canvas) {
        val step = view.measuredWidth / 4
        canvas.drawCircle(step.toFloat(), -100.0f, 400.0f, p1)
        canvas.drawCircle((2 * step).toFloat(), -150.0f, 350.0f, p2)
        canvas.drawCircle((3 * step).toFloat(), -200.0f, 450.0f, p3)
        canvas.drawCircle((4 * step).toFloat(), -100.0f, 200.0f, p4)
    }
}
