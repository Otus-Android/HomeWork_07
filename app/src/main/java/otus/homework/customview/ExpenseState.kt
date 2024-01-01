package otus.homework.customview

import android.os.Parcel
import android.os.Parcelable
import android.view.View

class ExpenseState(superState: Parcelable?) : View.BaseSavedState(superState) {

    var points: List<Float> = listOf()

    override fun writeToParcel(out: Parcel, flags: Int) {
        super.writeToParcel(out, flags)
        out.writeList(points)
    }
}