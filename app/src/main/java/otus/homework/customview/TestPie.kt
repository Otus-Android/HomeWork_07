package otus.homework.customview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.core.graphics.values
import kotlin.math.*

class TestPie @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAtr: Int = 0
) : View(context, attrs, defStyleAtr) {

    private val biasMatrix = Matrix().apply {
        setTranslate(10F, -10F)
    }

    private val pathList = List(12) { Path() }

    private val segmentPaint: Paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val corePaint: Paint = Paint().apply {
        isAntiAlias = true
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas?) {
        if (canvas == null) return
        if (isTouched) {
            biasMatrix.setTranslate(10f, -10f)
            pathList[0].transform(biasMatrix)
        } else {
            pathList[0].rewind()
            biasMatrix.reset()
            pathList[0].transform(biasMatrix)
            pathList[0].addRect(50f, 100f, 100f, 200f, Path.Direction.CW)
        }

        canvas.drawPath(pathList[0], segmentPaint)
    }


    private var isTouched = false

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        when(event?.action) {
            MotionEvent.ACTION_DOWN -> {
                isTouched = true
                this.invalidate()
                return true
            }
            MotionEvent.ACTION_UP -> {
                isTouched = false
                invalidate()
                return true
            }
        }
        return false
    }

}