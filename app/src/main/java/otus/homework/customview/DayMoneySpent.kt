package otus.homework.customview

import android.os.Parcel
import android.os.Parcelable
import java.util.*

data class DayMoneySpent(
//    val items: ArrayList<Item>?,
    var totalAmount: Int,
    var date: Long
    ): Parcelable {
    constructor(parcel: Parcel) : this(
//        parcel.createTypedArrayList(Item.CREATOR),
        parcel.readInt(),
        parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
//        parcel.writeTypedList(items)
        parcel.writeInt(totalAmount)
        parcel.writeLong(date)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DayMoneySpent> {
        override fun createFromParcel(parcel: Parcel): DayMoneySpent {
            return DayMoneySpent(parcel)
        }

        override fun newArray(size: Int): Array<DayMoneySpent?> {
            return arrayOfNulls(size)
        }
    }

}
