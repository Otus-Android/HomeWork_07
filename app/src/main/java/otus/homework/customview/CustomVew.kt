package otus.homework.customview

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View

open class CustomVew
@JvmOverloads
constructor(context: Context, attributeSet: AttributeSet? = null) : View(context, attributeSet) {
  lateinit var listPayload: List<PayloadUiModel>

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    val wMode = MeasureSpec.getMode(widthMeasureSpec)
    val wSize = MeasureSpec.getSize(widthMeasureSpec)
    val hSize = MeasureSpec.getSize(heightMeasureSpec)

    when (wMode) {
      MeasureSpec.EXACTLY -> {
        setMeasuredDimension(wSize, hSize)
      }
      MeasureSpec.AT_MOST -> {
        setMeasuredDimension(wSize, hSize)
      }
      MeasureSpec.UNSPECIFIED -> {
        setMeasuredDimension(wSize, hSize)
      }
    }
  }

  override fun onSaveInstanceState(): Parcelable {
    val superState = super.onSaveInstanceState()
    val bundle =
        Bundle().apply {
          putParcelable(SUPER_STATE_KEY, superState)
          putParcelableArray(PAYLOADS_KEY, listPayload.toTypedArray())
        }
    return bundle
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    var viewState = state
    if (viewState is Bundle) {
      viewState.apply {
        viewState = getParcelable(SUPER_STATE_KEY)
        getParcelableArrayList<PayloadUiModel>(PAYLOADS_KEY)?.let { listPayload = it }
      }
    }
    super.onRestoreInstanceState(viewState)
  }

  fun setValues(payloads: List<PayloadUiModel>) {
    listPayload = payloads
    requestLayout()
    invalidate()
  }

  companion object {
    private const val SUPER_STATE_KEY = "superState"
    const val PAYLOADS_KEY = "listPayloads"
  }
}
