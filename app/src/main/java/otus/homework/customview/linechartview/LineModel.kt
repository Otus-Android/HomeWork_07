package otus.homework.customview.linechartview

import android.annotation.SuppressLint
import android.graphics.Paint
import android.graphics.PointF
import android.os.Parcel
import android.os.Parcelable
import otus.homework.customview.utils.getLocalDateFromLong
import otus.homework.customview.utils.localDateToLong
import java.time.LocalDate

data class LineModel(
    val category: String,
    val positions: MutableList<DayPosition>,
    val paint: Paint
) : Parcelable {

    constructor(parcel: Parcel) : this(
        category = parcel.readString().orEmpty(),
        positions = parcel.readArrayList(null) as? MutableList<DayPosition>
            ?: mutableListOf<DayPosition>(),
        paint = Paint().apply { color = parcel.readInt() }
    )

    override fun describeContents(): Int {
        return 0
    }

    @SuppressLint("NewApi")
    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(category)
        dest?.writeParcelableList(positions, 0)
        dest?.writeInt(paint.color)
    }

    companion object CREATOR : Parcelable.Creator<LineModel> {
        override fun createFromParcel(source: Parcel): LineModel {
            return LineModel(source)
        }

        override fun newArray(size: Int): Array<LineModel?> {
            return arrayOfNulls(size)
        }
    }
}

data class DayPosition(
    var amount: Double,
    val day: LocalDate,
    val position: PointF = PointF()
) : Parcelable {

    constructor(parcel: Parcel) : this(
        amount = parcel.readDouble(),
        day = getLocalDateFromLong(parcel.readLong()),
        position = PointF(parcel.readFloat(), parcel.readFloat())
    )

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeDouble(amount)
        dest?.writeLong(localDateToLong(day))
        dest?.writeFloat(position.x)
        dest?.writeFloat(position.y)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DayPosition> {
        override fun createFromParcel(source: Parcel): DayPosition {
            return DayPosition(source)
        }

        override fun newArray(size: Int): Array<DayPosition?> {
            return arrayOfNulls(size)
        }
    }
}




















