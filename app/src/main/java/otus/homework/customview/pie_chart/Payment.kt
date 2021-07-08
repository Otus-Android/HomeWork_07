package otus.homework.customview.pie_chart

import android.os.Parcel
import android.os.Parcelable
import kotlinx.serialization.Serializable

@Serializable
data class Payment(
    val id: Int = 0,
    val name: String = "",
    val amount: Int = 0,
    val category: String = "",
    val time: Long = 0L
) : Parcelable {
    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<Payment> {
            override fun createFromParcel(parcel: Parcel) = Payment(parcel)
            override fun newArray(size: Int) = arrayOfNulls<Payment>(size)
        }
    }

    private constructor(parcel: Parcel) : this(
        id = parcel.readInt(),
        name = parcel.readString() ?: "",
        amount = parcel.readInt(),
        category = parcel.readString() ?: "",
        time = parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeInt(amount)
        parcel.writeString(category)
        parcel.writeLong(time)
    }

    override fun describeContents() = 0
}
