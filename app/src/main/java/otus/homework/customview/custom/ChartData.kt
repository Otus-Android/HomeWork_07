package otus.homework.customview.custom

import android.os.Parcel
import android.os.Parcelable

data class ChartData(
    val id: Long,
    val name: String,
    val amount: Int,
    val category: String,
    val time: Long
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString().toString(),
        parcel.readInt(),
        parcel.readString().toString(),
        parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(name)
        parcel.writeInt(amount)
        parcel.writeString(category)
        parcel.writeLong(time)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ChartData> {
        override fun createFromParcel(parcel: Parcel): ChartData {
            return ChartData(parcel)
        }

        override fun newArray(size: Int): Array<ChartData?> {
            return arrayOfNulls(size)
        }
    }
}
