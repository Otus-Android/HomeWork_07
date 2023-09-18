package otus.homework.customview.piechart

import android.os.Parcel
import android.os.Parcelable
import android.view.View

internal class PieChartSavedState : View.BaseSavedState {
    var centerText: String? = null
    var sectionList: List<PieChartSection> = emptyList()

    constructor(superState: Parcelable?) : super(superState)

    private constructor(source: Parcel) : super(source) {
        centerText = source.readString()

        source.readList(sectionList, ClassLoader.getSystemClassLoader())
    }

    override fun writeToParcel(out: Parcel?, flags: Int) {
        super.writeToParcel(out, flags)
        out?.writeString(centerText)
        out?.writeList(sectionList)
    }

    companion object CREATOR : Parcelable.Creator<PieChartSavedState> {
        override fun createFromParcel(parcel: Parcel): PieChartSavedState {
            return PieChartSavedState(parcel)
        }

        override fun newArray(size: Int): Array<PieChartSavedState?> {
            return arrayOfNulls(size)
        }
    }
}