package otus.homework.customview.pieChart

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import otus.homework.customview.ViewInfo
import java.text.NumberFormat
import java.util.*

class PieChartCenter(private val totalAmount: Float) {

    private var viewInfo: ViewInfo? = null

    var selectedAmount: Float? = null

    private val circlePaint = Paint().apply {
        flags = Paint.ANTI_ALIAS_FLAG
        style = Paint.Style.FILL
        color = Color.GRAY
    }

    private val textPaint = Paint().apply {
        flags = Paint.ANTI_ALIAS_FLAG
        textSize = 80f
        style = Paint.Style.FILL
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    private val smallTextSize = Paint().apply {
        flags = Paint.ANTI_ALIAS_FLAG
        textSize = 40f
        style = Paint.Style.FILL
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    /** Функция которая отрисует сентральный круг и напишет текст */
    fun draw(canvas: Canvas, viewInfo: ViewInfo) {
        this.viewInfo = viewInfo

        drawCircle(canvas, circlePaint)
        drawText(canvas, textPaint)
        drawSelectedSectorAmount(canvas, smallTextSize)
    }

    /** функция отрисовки круга*/
    private fun drawCircle(canvas: Canvas, paint: Paint) {
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
        viewInfo?.let {
            canvas.drawText(
                convertAmountToText(totalAmount.toInt()),
                it.getCenterX().toFloat(),
                it.getCenterY().toFloat() + 40f,
                paint
            )
        }
    }

    /** Функция которая отрисует сумму выбранного сектора*/
    private fun drawSelectedSectorAmount(canvas: Canvas, paint: Paint) {
        viewInfo?.let {
            canvas.drawText(
                convertAmountToText(selectedAmount?.toInt()),
                it.getCenterX().toFloat(),
                it.getCenterY().toFloat() + 140f,
                paint
            )
        }
    }

    /** Функция перевода суммы в удобный вид */
    private fun convertAmountToText(amount: Int?): String {
        val format = NumberFormat.getCurrencyInstance()
        format.maximumFractionDigits = 0
        format.currency = Currency.getInstance(Locale.getDefault())
        return amount?.let {
            format.format(it)
        } ?: ""
    }
}