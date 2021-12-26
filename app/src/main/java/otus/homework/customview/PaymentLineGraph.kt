package otus.homework.customview

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class ListPaymentLineGraph(
    var listLineGraph: MutableList<PaymentLineGraph>,
    val superSaveState: Parcelable?
): Parcelable

@Parcelize
data class PaymentLineGraph(
    val amount: Int,
    val category: String,
    val date: Long,
): Parcelable {

    @IgnoredOnParcel
    val calendar: Calendar = Calendar.getInstance()

    @IgnoredOnParcel
    var day: Int =  calendar.apply {
        timeInMillis = (date*1000) }.get(Calendar.DAY_OF_MONTH)

    @IgnoredOnParcel
    var dayInMonth: Int =  calendar.apply {
        timeInMillis = (date*1000) }.getActualMaximum(Calendar.DAY_OF_MONTH)

    @IgnoredOnParcel
    var month: String? =  calendar.apply {
        timeInMillis = (date*1000) }.getDisplayName(Calendar.MONTH, Calendar.LONG_STANDALONE, Locale.getDefault())

}
