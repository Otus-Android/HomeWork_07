package otus.homework.customview.chartview

import android.graphics.Paint
import android.graphics.PointF
import android.os.Parcel
import android.os.Parcelable

data class PieModel(
    val name: String,
    var amount: Double,
    var startAngle: Float = 0f,
    var sweepAngle: Float = 0f,
    var categoryLocation: PointF = PointF(),
    val paint: Paint
) : Parcelable {

    constructor(parcel: Parcel) : this(
        name = parcel.readString().orEmpty(),
        amount = parcel.readDouble(),
        startAngle = parcel.readFloat(),
        sweepAngle = parcel.readFloat(),
        categoryLocation = PointF(parcel.readFloat(), parcel.readFloat()),
        paint = Paint().apply {
            color = parcel.readInt()
        }
    )

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(name)
        dest.writeDouble(amount)
        dest.writeFloat(startAngle)
        dest.writeFloat(sweepAngle)
        dest.writeFloat(categoryLocation.x)
        dest.writeFloat(categoryLocation.y)
        dest.writeInt(paint.color)
    }

    companion object CREATOR : Parcelable.Creator<PieModel> {
        override fun createFromParcel(source: Parcel): PieModel {
            return PieModel(source)
        }

        override fun newArray(size: Int): Array<PieModel?> {
            return arrayOfNulls(size)
        }
    }
}
