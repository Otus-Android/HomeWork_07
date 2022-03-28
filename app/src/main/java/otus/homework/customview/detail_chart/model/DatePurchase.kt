package otus.homework.customview.detail_chart.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
class DatePurchase(
    var amount: Int,
    val time: Calendar,
    val stringTime: String
): Parcelable