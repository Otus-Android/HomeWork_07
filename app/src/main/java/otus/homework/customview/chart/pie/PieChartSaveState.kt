package otus.homework.customview.chart.pie

import android.os.Parcel
import android.os.Parcelable
import android.view.View

internal class PieChartSaveState : View.BaseSavedState {
    var selectionPercentText: String? = null

    constructor(superState: Parcelable?) : super(superState)

    private constructor(source: Parcel) : super(source) {
        selectionPercentText = source.readString()
    }

    override fun writeToParcel(out: Parcel, flags: Int) {
        super.writeToParcel(out, flags)
        out.writeString(selectionPercentText)
    }

    companion object CREATOR : Parcelable.Creator<PieChartSaveState> {
        override fun createFromParcel(parcel: Parcel): PieChartSaveState {
            return PieChartSaveState(parcel)
        }

        override fun newArray(size: Int): Array<PieChartSaveState?> {
            return arrayOfNulls(size)
        }
    }
}