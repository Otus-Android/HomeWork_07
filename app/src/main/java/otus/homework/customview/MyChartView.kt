package otus.homework.customview

import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View


class MyChartView : View {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attributeSet: AttributeSet?) : super(context, attributeSet)

    private val rec = RectF()
    private val color = arrayOf(
        Color.RED,
        Color.BLUE,
        -0x886699,
        -0x11BB88,
        Color.CYAN,
        Color.GRAY,
        Color.YELLOW,
        Color.MAGENTA,
        Color.GREEN
    )
    private val pieData = ChartModel().pieData
    private val sumData = pieData.map { it -> it.value }.sum()

    init {
        rec.left = 0f
        rec.top = 0f
        isSaveEnabled = true
    }

    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        bundle.putParcelable("superState", super.onSaveInstanceState())
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        var viewState = state
        if (viewState is Bundle) {
            viewState = viewState.getParcelable("superState")
        }
        super.onRestoreInstanceState(viewState)
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val finalWidth = getMeasuredWidth() * 3 / 4
        val finalHeight = getMeasuredWidth() * 3 / 4
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        System.out.println("widthMode $widthMode heightMode $heightMode")
        System.out.println("widthMode ${MeasureSpec.EXACTLY} heightMode ${MeasureSpec.AT_MOST}")
        setMeasuredDimension(finalWidth, finalHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val midHeight = height / 2f
        val midWidth = width / 2f
        val startLeft = width * 0.1f
        val startTop = height * 0.1f
        var startAngle = 0f
        var sweepAngle = 0f
        canvas.drawARGB(230, 400, 150, 50)

        val p = Paint()
        p.isAntiAlias = true
        p.color = Color.RED
        p.style = Paint.Style.STROKE
        p.strokeWidth = 40f
        p.color = Color.BLACK

        val pText = Paint()
        pText.textSize = 20f
        pText.style = Paint.Style.FILL_AND_STROKE
        pText.setStrokeWidth(2f)

        canvas.translate(0f, height / 4f)
        var j = 0
        pieData.forEach { s, i ->
            sweepAngle = (i * 360f) / sumData
            p.color = color[j++ % 9]
            canvas.drawArc(startLeft, startTop, midWidth, midHeight, startAngle, sweepAngle, false, p)
            canvas.translate(midWidth * 1.2f, j * 50f)
            textDraw(canvas, pText, p, s)
            canvas.translate(-midWidth * 1.2f, -j * 50f)
            startAngle += sweepAngle
        }
    }

    private fun textDraw(canvas: Canvas, p: Paint, pBack: Paint, text: String) {
        val rect = Rect(20, 20, 170, 23)
        canvas.drawRect(rect, pBack)
        System.out.println("getTextLocale() $p.getTextLocale()")
        canvas.drawText(text, 10f, 25f, p)
    }
}