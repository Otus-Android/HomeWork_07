package otus.homework.customview.piechart

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import otus.homework.customview.R
import otus.homework.customview.data.PurchasesData
import kotlin.math.sqrt

class PieChartView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val pieChartData = PieChartData()

    private val pieChartPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val pieChartPaineStroke = Paint().apply {
        style = Paint.Style.STROKE
        isAntiAlias = true
        color = Color.WHITE
        strokeWidth = 5f
    }

    private val innerPieChartCircle = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = 30f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    private val textPaintDesc = Paint().apply {
        color = Color.BLACK
        typeface = Typeface.DEFAULT_BOLD
        textSize = 40f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }
    private val path = Path()

    private val purchasesData = PurchasesData()
    private var selectedId: String? = null
    private val piecesColors: IntArray = context.resources.getIntArray(R.array.piecesColors)
    private var currentPieceName: String = ""
    private var currentPieceValue: String = ""

    private val defaultSize = 500
    private var currentSize = defaultSize
    private val padding = 20f

    var onOrderClick: ((String) -> (Unit))? = null

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)
        val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)

        var size = minOf(heightSize, widthSize)

        if (size < defaultSize) size = defaultSize

        currentSize = if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(defaultSize, defaultSize)
            defaultSize
        } else {
            setMeasuredDimension(size, size)
            size
        }
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        var currentAngle = 0f
        if (pieChartData.pieces.isEmpty()) {
            var colorIterator = piecesColors.iterator()

            for (purchase in purchasesData.getPurchasesForPieChart(context)) {

                if (!colorIterator.hasNext()) colorIterator = piecesColors.iterator()
                pieChartPaint.color = colorIterator.next()

                val isCurrent = purchase.key.id == selectedId
                val currentPadding = if (isCurrent) 100f else padding
                currentPieceName = if (isCurrent) purchase.key.name else currentPieceName
                currentPieceValue = if (isCurrent) purchase.key.amount else currentPieceValue
                currentAngle = drawPieceOfPieChart(
                    canvas,
                    currentPadding,
                    currentSize,
                    currentAngle,
                    purchase.value,
                    purchase.key.name,
                    currentPieceName,
                    currentPieceValue
                )

                pieChartData.addPiece(
                    purchase.key.id,
                    purchase.key.name,
                    purchase.key.category,
                    pieChartPaint.color,
                    purchase.key.amount,
                    purchase.value,
                )


            }
        } else {
            for (piece in pieChartData.pieces) {
                pieChartPaint.color = piece.color

                val isCurrent = piece.id == selectedId
                val currentPadding = if (isCurrent) 100f else padding
                currentPieceName = if (isCurrent) piece.name else currentPieceName
                currentPieceValue = if (isCurrent) piece.amount else currentPieceValue

                currentAngle = drawPieceOfPieChart(
                    canvas,
                    currentPadding,
                    currentSize,
                    currentAngle,
                    piece.currentAngle,
                    piece.name,
                    currentPieceName,
                    currentPieceValue
                )
            }
        }
    }

    private fun drawPieceOfPieChart(
        canvas: Canvas,
        currentPadding: Float,
        currentSize: Int,
        currentAngle: Float,
        currentPieceAngle: Float,
        pieceName: String,
        currentPieceName: String,
        currentPieceValue: String
    ): Float {
        canvas.drawArc(
            currentPadding,
            currentPadding,
            currentSize - currentPadding,
            currentSize - currentPadding,
            currentAngle,
            currentPieceAngle,
            true,
            pieChartPaint
        )
        canvas.drawArc(
            padding,
            padding,
            currentSize - padding,
            currentSize - padding,
            currentAngle,
            currentPieceAngle,
            true,
            pieChartPaineStroke
        )

        path.reset();
        path.addArc(
            currentPadding,
            currentPadding,
            currentSize - currentPadding,
            currentSize - currentPadding,
            currentAngle,
            currentPieceAngle
        )

        canvas.drawTextOnPath(
            pieceName,
            path,
            0F,
            0f,
            textPaint
        )
        canvas.drawCircle(
            (width / 2).toFloat(),
            (height / 2).toFloat(), defaultSize / 2f, innerPieChartCircle
        )


        canvas.drawText(
            currentPieceName,
            (width / 2).toFloat(),
            (height / 2).toFloat(),
            textPaintDesc
        )
        canvas.drawText(
            currentPieceValue,
            (width / 2).toFloat(),
            (height / 2).toFloat() + 50,
            textPaintDesc
        )
        return currentAngle + currentPieceAngle
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val centerPieChartX = event.x - currentSize / 2f
            val centerPieChartY = event.y - currentSize / 2f
            val pieChartRadius =
                sqrt(centerPieChartX * centerPieChartX.toDouble() + centerPieChartY * centerPieChartY.toDouble())
            if (pieChartRadius < currentSize * .5) {
                val touchAngle = pieChartData.getAngleByXY(centerPieChartX, centerPieChartY)
                var currentAngle = 0f

                if (pieChartData.pieces.isEmpty()) {
                    for (purchase in purchasesData.getPurchasesForPieChart(context)) {

                        if (touchAngle > currentAngle && touchAngle < currentAngle + purchase.value) {
                            if (selectedId != purchase.key.id) {
                                selectedId = purchase.key.id

                                requestLayout()
                                invalidate()
                                onOrderClick?.invoke(purchase.key.category)
                            }
                            return true
                        }
                        currentAngle += purchase.value
                    }
                } else {
                    for (piece in pieChartData.pieces) {
                        if (touchAngle > currentAngle && touchAngle < currentAngle + piece.currentAngle) {
                            if (piece.id != selectedId) {
                                selectedId = piece.id

                                requestLayout()
                                invalidate()
                                onOrderClick?.invoke(piece.category)
                            }
                            return true
                        }
                        currentAngle += piece.currentAngle
                    }
                }
            }
        }
        return false
    }


    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        bundle.putSerializable("pieChartData", pieChartData)
        bundle.putParcelable("supPieChartData", super.onSaveInstanceState())
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        var viewState = state
        if (viewState is Bundle) {
            val restoreState = viewState.getSerializable("pieChartData") as PieChartData
            viewState = viewState.getParcelable("supPieChartData")

        }
        super.onRestoreInstanceState(viewState)
    }

}