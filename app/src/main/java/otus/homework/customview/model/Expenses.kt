package otus.homework.customview.model

import android.os.Parcel
import android.os.Parcelable

data class Expenses(
    val id: Int,
    val name: String,
    val amount: Int,
    val category: String,
    val time: Long
) : Parcelable{
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readString() ?:"",
        parcel.readLong()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeInt(amount)
        parcel.writeString(category)
        parcel.writeLong(time)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Expenses> {
        override fun createFromParcel(parcel: Parcel): Expenses {
            return Expenses(parcel)
        }

        override fun newArray(size: Int): Array<Expenses?> {
            return arrayOfNulls(size)
        }
    }

}