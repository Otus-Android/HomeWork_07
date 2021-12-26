package otus.homework.customview

import android.graphics.Path
import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class ListPayment(
    var listPayment: MutableList<PaymentPieChart>,
    val superSaveState: Parcelable?
): Parcelable

@Parcelize
data class PaymentPieChart(
    val amountSum: Int,
    val category: String,
    val arc: Float,
): Parcelable {
    @IgnoredOnParcel
    val path = Path()
}


