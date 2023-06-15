package otus.homework.customview.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import otus.homework.customview.dp
import otus.homework.customview.model.Expenses
import otus.homework.customview.model.ExpensesByCategoryState
import otus.homework.customview.model.ExpensesState

class LineChartView@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {
    private var mPaint: Paint? = null
    private var mIsInit = false
    private var mPath: Path? = null

    private var mOriginY = 0f
    private var mOriginX = 0f
    private var mWidth = 0
    private var mHeight = 0
    private var mXUnit = 0f
    private var mYUnit = 0f
    private var mBlackPaint: Paint? = null
    private var mDataPoints: List<Float> = listOf()

    private fun init() {
        mPaint = Paint()
        mPath = Path()
        mWidth = width
        mHeight = height
        mXUnit = (mWidth / 10).toFloat() //for 10 plots on x axis, 2 kept for padding;
        mYUnit = (mHeight / 20).toFloat()
        mOriginX = mXUnit
        mOriginY = mHeight - mYUnit
        mBlackPaint = Paint()
        mIsInit = true
    }

    fun setData(dataPoints: List<Float>) {
        mDataPoints = dataPoints
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        var width = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        var height = MeasureSpec.getSize(heightMeasureSpec)

        when (widthMode) {
            MeasureSpec.AT_MOST, MeasureSpec.UNSPECIFIED -> if (width > height) {
                width = (mDataPoints.size) * 50.dp(context).toInt()
            }
            MeasureSpec.EXACTLY -> { /* leave exactly width */
            }
        }
        when (heightMode) {
            MeasureSpec.AT_MOST, MeasureSpec.UNSPECIFIED -> if (height > width) {
                height = (mDataPoints.size) * 50.dp(context).toInt()
            }
            MeasureSpec.EXACTLY -> { /* leave exactly height */
            }
        }

        setMeasuredDimension(width, height)
    }

    private fun drawAxis(canvas: Canvas, paint: Paint) {
        canvas.drawLine(mXUnit, mYUnit, mXUnit, mHeight - 10f, paint) //y-axis
        canvas.drawLine(
            10f, mHeight - mYUnit,
            mWidth - mXUnit, mHeight - mYUnit, paint
        ) //x-axis
    }
    private fun drawGraphPlotLines(canvas: Canvas, path: Path, paint: Paint) {
        var originX = mXUnit
        val originY = mHeight - mYUnit
        mPath!!.moveTo(originX, originY) //shift origin to graph's origin
        for (i in mDataPoints.indices) {
            mPath!!.lineTo(originX + mXUnit, originY - mDataPoints.get(i) * mYUnit)
            canvas.drawCircle(
                originX + mXUnit, originY - mDataPoints.get(i) * mYUnit, 5f, paint
            )
            originX += mXUnit
        } //end for
        canvas.drawPath(mPath!!, paint)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        init()
        mBlackPaint!!.color = Color.BLACK
        mBlackPaint!!.style = Paint.Style.STROKE
        mBlackPaint!!.strokeWidth = 10f
        mPaint!!.style = Paint.Style.STROKE
        mPaint!!.strokeWidth = 10f
        mPaint!!.color = Color.BLUE
        drawAxis(canvas!!, mBlackPaint!!)
        drawGraphPlotLines(canvas, mPath!!, mPaint!!)
       // drawGraphPaper(canvas, mBlackPaint!!)
        //drawTextOnXaxis(canvas, mBlackPaint)
        //drawTextOnYaxis(canvas, mBlackPaint)
    }

    override fun onSaveInstanceState(): Parcelable {
        return ExpensesState(super.onSaveInstanceState()).apply {
            this.points = this@LineChartView.mDataPoints
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is ExpensesState) {
            mDataPoints = state.points
        }
        super.onRestoreInstanceState(state)
    }
}