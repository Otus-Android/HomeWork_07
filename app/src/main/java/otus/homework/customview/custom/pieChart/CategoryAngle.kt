package otus.homework.customview.custom.pieChart

import android.os.Parcel
import android.os.Parcelable

data class CategoryAngle (
    val startAngle: Float,
    val endAngle: Float
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readFloat(),
        parcel.readFloat()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeFloat(startAngle)
        parcel.writeFloat(endAngle)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CategoryAngle> {
        override fun createFromParcel(parcel: Parcel): CategoryAngle {
            return CategoryAngle(parcel)
        }

        override fun newArray(size: Int): Array<CategoryAngle?> {
            return arrayOfNulls(size)
        }
    }
}