package otus.homework.customview.model

import android.os.Parcel
import android.os.Parcelable
import android.view.View

class ExpensesState : View.BaseSavedState {

    var points: List<Float> = listOf()

    constructor(superState: Parcelable?) : super(superState)

    private constructor(parcel: Parcel) : super(parcel) {
        points = listOf()
        parcel.readFloatArray(points.toFloatArray())
    }

    override fun writeToParcel(out: Parcel, flags: Int) {
        super.writeToParcel(out, flags)
        out.writeList(points)
    }
}