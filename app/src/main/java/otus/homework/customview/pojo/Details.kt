package otus.homework.customview.pojo

import android.os.Parcel
import android.os.Parcelable

data class Details (
    val name: String?,
    val amount: Int,
    val date: Long, // to days
    ) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readInt(),
        parcel.readLong(),
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeInt(amount)
        parcel.writeLong(date)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Details> {
        override fun createFromParcel(parcel: Parcel): Details {
            return Details(parcel)
        }

        override fun newArray(size: Int): Array<Details?> {
            return arrayOfNulls(size)
        }
    }
}