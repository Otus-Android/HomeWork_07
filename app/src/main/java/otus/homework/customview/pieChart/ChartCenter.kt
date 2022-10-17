package otus.homework.customview.pieChart

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

class ChartCenter(private val totalAmount: Float) {

    private var viewInfo: ViewInfo? = null

    var selectedAmount: Float? = null

    /** Функция которая отрисует сентральный круг и напишет текст */
    fun draw(canvas: Canvas, paint: Paint, viewInfo: ViewInfo) {
        this.viewInfo = viewInfo

        drawCircle(canvas, paint)
        drawText(canvas, paint)
    }

    /** функция отрисовки круга*/
    private fun drawCircle(canvas: Canvas, paint: Paint) {

        paint.style = Paint.Style.FILL
        paint.color = Color.GRAY

        viewInfo?.let {

            val radius = it.getViewSize() * 0.25f
            canvas.drawCircle(
                it.getCenterX().toFloat(),
                it.getCenterY().toFloat(),
                radius,
                paint
            )
        }

    }

    /** функция отрисовки текста в круге*/
    private fun drawText(canvas: Canvas, paint: Paint) {

        paint.textSize = 80f
        paint.style = Paint.Style.FILL
        paint.color = Color.BLACK
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.DEFAULT_BOLD

        viewInfo?.let {

            val format = NumberFormat.getCurrencyInstance()
            format.maximumFractionDigits = 0
            format.currency = Currency.getInstance(Locale.getDefault())
            val text = format.format(totalAmount)

            canvas.drawText(
                text,
                it.getCenterX().toFloat(),
                it.getCenterY().toFloat() + 40f,
                paint
            )

            selectedAmount?.let { amount ->
                val selectedAmountText = format.format(amount)
                paint.textSize = 40f
                canvas.drawText(
                    selectedAmountText,
                    it.getCenterX().toFloat(),
                    it.getCenterY().toFloat() + 140f,
                    paint
                )
            }

        }


    }

}