package otus.homework.customview.data.graphics

import android.graphics.Rect
import android.view.View
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin


object PiePoint {
  fun getPair(angle: Float, size: Float): Pair<Float, Float> {
    return Pair(
      cos(Math.toRadians(angle.toDouble())).toFloat() * size * .45f,
      sin(Math.toRadians(angle.toDouble())).toFloat() * size * .45f
    )
  }

  fun angleByPoint(x: Float, y: Float, view: View): Float {
    val res = Math.toDegrees(atan2(y - view.height/2 + 20, x - view.width/2).toDouble()).toFloat()
    return if (res < 0) res + 360 else res
  }

  fun locatedInChartWith(x: Float, y: Float, view: View): Boolean {
//    val rect = Rect()
//    val location = IntArray(2)
//    view.getDrawingRect(rect)
//    view.getLocationOnScreen(location)
//    rect.offset(location[0], location[1])
//    return rect.contains(x.toInt(), y.toInt()
    return (x - view.width/2).pow(2) + (y - view.height/2).pow(2) < (view.height/2.0 - 20).pow(2)
  }
}
