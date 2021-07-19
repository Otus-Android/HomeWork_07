package otus.homework.customview.chart_by_day

import android.os.Parcel
import android.os.Parcelable
import android.view.View

internal class DayCategoryPaymentSavedState : View.BaseSavedState {
    var items: List<DayCategoryPayment>? = null

    constructor(superState: Parcelable) : super(superState)

    constructor(source: Parcel) : super(source) {
        items?.let { source.readList(it, DayCategoryPayment::class.java.classLoader) }
    }

    override fun writeToParcel(out: Parcel, flags: Int) {
        super.writeToParcel(out, flags)
        out.writeList(items)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<DayCategoryPaymentSavedState> = object : Parcelable.Creator<DayCategoryPaymentSavedState> {
            override fun createFromParcel(source: Parcel): DayCategoryPaymentSavedState {
                return DayCategoryPaymentSavedState(source)
            }

            override fun newArray(size: Int): Array<DayCategoryPaymentSavedState> {
                return newArray(size)
            }
        }
    }
}