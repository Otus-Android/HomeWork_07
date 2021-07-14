package otus.homework.customview.pie_chart

import android.os.Parcel
import android.os.Parcelable

data class Category(
    val id: Int = 0,
    val payments: List<Payment> = emptyList()
) : Parcelable {
    val sum: Int = payments.sumOf { it.amount }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<Category> {
            override fun createFromParcel(parcel: Parcel) = Category(parcel)
            override fun newArray(size: Int) = arrayOfNulls<Category>(size)
        }
    }

    private constructor(parcel: Parcel) : this(
        id = parcel.readInt()
    ) {
        parcel.readList(payments, Payment::class.java.classLoader)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeList(payments)
    }

    override fun describeContents() = 0
}
