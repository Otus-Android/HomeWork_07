package otus.homework.customview.model

import android.text.format.DateUtils
import otus.homework.customview.extensions.convertToDayMillis
import java.util.*

data class Payload(
    val id: Int,
    val name: String,
    val amount: Int,
    val category: String,
    val time: Long
) {

    fun getDayMillis(): Long {
        val date = Date(getTimeInMillis())
        return date.convertToDayMillis()
    }

    private fun getTimeInMillis(): Long {
        return time * DateUtils.SECOND_IN_MILLIS
    }
}