package otus.homework.customview.data.graph

import android.database.DataSetObservable
import android.database.DataSetObserver
import android.graphics.RectF

abstract class ChartAdapter {
  abstract val count: Int
  abstract fun getItem(index: Int): Any
  abstract fun getY(index: Int): Float
  abstract fun getX(index: Int): Float

  private val observable = DataSetObservable()

  open val baseline: Float = 0f

  open val dataBounds: RectF
    get() {
      val count = count
      val hasBaseline = hasBaseline()
      var minY = Float.MAX_VALUE
      var maxY = - Float.MAX_VALUE
      var minX = Float.MAX_VALUE
      var maxX = - Float.MAX_VALUE
      for (i in 0 until count) {
        val x = getX(i)
        minX = minX.coerceAtMost(x)
        maxX = maxX.coerceAtLeast(x)
        val y = getY(i)
        minY = minY.coerceAtMost(y)
        maxY = maxY.coerceAtLeast(y)
      }
      return createRectF(minX, minY, maxX, maxY)
    }

  protected fun createRectF(left: Float, top: Float, right: Float, bottom: Float): RectF = RectF(left, top, right, bottom)

  open fun hasBaseline(): Boolean = false

  fun notifyDataSetChanged() = observable.notifyChanged()

  fun notifyDataSetInvalidated() = observable.notifyInvalidated()

  fun registerDataSetObserver(observer: DataSetObserver) = observable.registerObserver(observer)

  fun unregisterDataSetObserver(observer: DataSetObserver) = observable.unregisterObserver(observer)
}