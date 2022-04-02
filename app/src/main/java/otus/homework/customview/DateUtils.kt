package otus.homework.customview

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    val sdf = SimpleDateFormat("dd.MM")

    fun DateFromTimestamp(stamp:Int): String? {
        try {
            val mDate = Date(stamp.toLong()  * 1000)
            return sdf.format(mDate)
        } catch (e: Exception) {
            return e.toString()
        }
    }
}