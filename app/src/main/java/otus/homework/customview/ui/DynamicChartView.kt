package otus.homework.customview.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View

// lines
private const val LINE_WIDTH = 8f

class DynamicChartView @JvmOverloads constructor(
  context: Context,
  attributeSet: AttributeSet? = null,
  defStyleAttrs: Int = 0
) : View(context, attributeSet, defStyleAttrs) {

  private var model = DynamicChartModel(emptyList())

  private val paint = Paint()
    .apply {
      style = Paint.Style.STROKE
      strokeWidth = LINE_WIDTH
    }

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec)
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    model.lines.forEachIndexed { index, line ->
      paint.color = COLORS[index % COLORS.size]
      canvas.drawLine(0f, 100f * index, measuredWidth.toFloat(), measuredHeight.toFloat() - 100f * index, paint)
    }
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
