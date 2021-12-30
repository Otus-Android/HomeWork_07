package otus.homework.customview

import android.graphics.Path
import android.os.Parcel
import android.os.Parcelable

class PieCategory( val category: String?, val angle: Float) : Parcelable {
    lateinit var path: Path

    constructor(parc: Parcel) : this(
        parc.readString(), parc.readFloat()
    )
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(category)
        parcel.writeFloat(angle)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PieCategory> {
        override fun createFromParcel(parcel: Parcel): PieCategory {
            return PieCategory(parcel)
        }

        override fun newArray(size: Int): Array<PieCategory?> {
            return arrayOfNulls(size)
        }
    }
}