package otus.homework.customview.data.pie

import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import kotlin.random.Random

class ChartData {
  val segments = HashMap<String, ChartSegment>()
  var total = .0

  fun appendSegment(withTitle: String, andData: Double, ofColor: String? = null) {
    if (segments.containsKey(withTitle))
      segments[withTitle]?.let { it.data += andData }
    else
      ofColor?.let {
        segments[withTitle] = ChartSegment(withTitle, andData, 0f, 0f, PointF(), makePaint(it))
      } ?: run {
        segments[withTitle] = ChartSegment(withTitle, andData, 0f, 0f, PointF(), makePaint(null))
      }
    total += andData
  }

  private fun makePaint(forColor: String?): Paint {
    val paint = Paint().apply {
      forColor?.let {
        color = Color.parseColor(it)
      } ?: run {
        val value = Random(255)
        color = Color.argb(255, value.nextInt(), value.nextInt(), value.nextInt())
      }
      isAntiAlias = true
    }
    return paint
  }
}