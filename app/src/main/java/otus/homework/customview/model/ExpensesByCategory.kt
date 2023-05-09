package otus.homework.customview.model

import android.os.Parcel
import android.os.Parcelable

data class ExpensesByCategory(
    val category: String,
    val amount: Int
    ) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(category)
        parcel.writeInt(amount)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ExpensesByCategory> {
        override fun createFromParcel(parcel: Parcel): ExpensesByCategory {
            return ExpensesByCategory(parcel)
        }

        override fun newArray(size: Int): Array<ExpensesByCategory?> {
            return arrayOfNulls(size)
        }
    }
}