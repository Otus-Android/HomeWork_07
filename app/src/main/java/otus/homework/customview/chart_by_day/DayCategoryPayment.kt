package otus.homework.customview.chart_by_day

import android.os.Parcel
import android.os.Parcelable
import otus.homework.customview.pie_chart.Payment

data class DayCategoryPayment(
    val id: Int = 0,
    val payments: List<Payment> = emptyList(),
    val dayAndMonth: String = ""
) : Parcelable {

    val sum: Int = payments.sumOf { it.amount }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<DayCategoryPayment> {
            override fun createFromParcel(parcel: Parcel) = DayCategoryPayment(parcel)
            override fun newArray(size: Int) = arrayOfNulls<DayCategoryPayment>(size)
        }
    }

    private constructor(parcel: Parcel) : this(
        id = parcel.readInt(),
        dayAndMonth = parcel.readString() ?: ""
    ) {
        parcel.readList(payments, Payment::class.java.classLoader)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeList(payments)
        parcel.writeString(dayAndMonth)
    }

    override fun describeContents() = 0
}
