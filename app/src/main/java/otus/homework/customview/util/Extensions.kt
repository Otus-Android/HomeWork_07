package otus.homework.customview.util

import otus.homework.customview.data.model.Item
import java.text.SimpleDateFormat
import java.util.*

typealias ItemSegment = Float
typealias Category = String
typealias Count = Int
typealias Items = List<Item>

fun String.asResource(doWorkWith: (String?) -> Unit) {
  val content = this.javaClass.getResource(this)?.readText(Charsets.UTF_8)
  doWorkWith(content)
}

fun Long.toDay(): Long {
  val cal = Calendar.getInstance()
  cal.timeInMillis = this * 1000
  cal.set(Calendar.HOUR, 0)
  cal.set(Calendar.MINUTE, 0)
  cal.set(Calendar.SECOND, 0)

  return cal.timeInMillis / 1000
}

fun Long.toDateString(): String {
  val fmt = SimpleDateFormat("dd MMM", Locale.getDefault())
  return fmt.format(Date(this * 1000))
}

