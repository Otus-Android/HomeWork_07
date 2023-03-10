package otus.homework.customview.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.CornerPathEffect
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.Path
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View

// lines
private const val LINE_WIDTH = 8f
private const val CORNER_RADIUS = 8f

class DynamicChartView @JvmOverloads constructor(
  context: Context,
  attributeSet: AttributeSet? = null,
  defStyleAttrs: Int = 0
) : View(context, attributeSet, defStyleAttrs) {

  private var model = DynamicChartModel(emptyList())

  private val paint = Paint(ANTI_ALIAS_FLAG)
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

  fun updateData(model: DynamicChartModel) {
    this.model = model
    invalidate()
  }

  override fun onSaveInstanceState(): Parcelable {
    return DynamicChartSavedState(super.onSaveInstanceState())
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    super.onRestoreInstanceState(state)
    if (state is DynamicChartSavedState) {
      model = state.model
    }
  }

  private fun DynamicChartPoint.pointX() : Float = datePosition * measuredWidth

  private fun DynamicChartPoint.pointY() : Float = measuredHeight - amountPosition * measuredHeight

  private inner class DynamicChartSavedState : BaseSavedState {

    internal val model: DynamicChartModel

    constructor(source: Parcelable?) : super(source) {
      model = this@DynamicChartView.model
    }

    constructor(`in`: Parcel) : super(`in`) {
      model = `in`.readParcelable(DynamicChartModel::class.java.classLoader) ?: DynamicChartModel(emptyList())
    }

    override fun writeToParcel(out: Parcel?, flags: Int) {
      super.writeToParcel(out, flags)
      out?.writeParcelable(model, 0)
    }

    @JvmField
    val CREATE: Parcelable.Creator<DynamicChartSavedState> = object : Parcelable.Creator<DynamicChartSavedState> {

      override fun createFromParcel(source: Parcel): DynamicChartSavedState {
        return DynamicChartSavedState(source)
      }

      override fun newArray(size: Int): Array<DynamicChartSavedState?> {
        return arrayOfNulls(size)
      }
    }
  }
}
