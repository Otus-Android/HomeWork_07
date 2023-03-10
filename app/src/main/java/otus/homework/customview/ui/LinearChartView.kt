package otus.homework.customview.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.CornerPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View

// lines
private const val LINE_WIDTH = 8f
private const val CORNER_RADIUS = 8f

class LinearChartView @JvmOverloads constructor(
  context: Context,
  attributeSet: AttributeSet? = null,
  defStyleAttrs: Int = 0
) : View(context, attributeSet, defStyleAttrs) {

  private var model = LinearChartModel(emptyList())

  private val paint = Paint()
    .apply {
      style = Paint.Style.STROKE
      strokeWidth = LINE_WIDTH
      pathEffect = CornerPathEffect(CORNER_RADIUS)
    }

  private val path = Path()

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec)
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    model.lines.forEachIndexed { index, line ->
      paint.color = COLORS[index % COLORS.size]

      val points = line.points

      if (points.isEmpty()) {
        return@forEachIndexed
      }

      path.reset()

      val firstPoint = points[0]

      val prevPointX = firstPoint.pointX()
      var prevPointY = firstPoint.pointY()
      if (prevPointX == 0f) {
        path.moveTo(prevPointX, prevPointY)
      } else {
        path.moveTo(0f, measuredHeight.toFloat())
        path.addLineAndMove(prevPointX, measuredHeight.toFloat())
        path.addLineAndMove(prevPointX, prevPointY)
      }

      points.drop(1).forEach {
        val pointX = it.pointX()
        val pointY = it.pointY()
        path.addLineAndMove(pointX, prevPointY)
        path.addLineAndMove(pointX, pointY)
        prevPointY = pointY
      }

      val lastPoint = points.last()
      path.addLineAndMove(measuredWidth.toFloat(), lastPoint.pointY())

      path.close()

      canvas.drawPath(path, paint)
    }
  }

  private fun Path.addLineAndMove(x: Float, y: Float) {
    lineTo(x, y)
    moveTo(x, y)
  }

  fun updateData(model: LinearChartModel) {
    this.model = model
    invalidate()
  }

  override fun onSaveInstanceState(): Parcelable {
    return LinearChartSavedState(super.onSaveInstanceState())
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    super.onRestoreInstanceState(state)
    if (state is LinearChartSavedState) {
      model = state.model
    }
  }

  private fun LinearChartPoint.pointX() : Float = datePosition * measuredWidth

  private fun LinearChartPoint.pointY() : Float = measuredHeight - amountPosition * measuredHeight

  private inner class LinearChartSavedState : BaseSavedState {

    internal val model: LinearChartModel

    constructor(source: Parcelable?) : super(source) {
      model = this@LinearChartView.model
    }

    constructor(`in`: Parcel) : super(`in`) {
      model = `in`.readParcelable(LinearChartModel::class.java.classLoader) ?: LinearChartModel(emptyList())
    }

    override fun writeToParcel(out: Parcel?, flags: Int) {
      super.writeToParcel(out, flags)
      out?.writeParcelable(model, 0)
    }

    @JvmField
    val CREATE: Parcelable.Creator<LinearChartSavedState> = object : Parcelable.Creator<LinearChartSavedState> {

      override fun createFromParcel(source: Parcel): LinearChartSavedState {
        return LinearChartSavedState(source)
      }

      override fun newArray(size: Int): Array<LinearChartSavedState?> {
        return arrayOfNulls(size)
      }
    }
  }
}
