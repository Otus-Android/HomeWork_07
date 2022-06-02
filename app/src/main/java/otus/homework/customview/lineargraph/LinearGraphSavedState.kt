package otus.homework.customview.lineargraph

import android.os.Parcel
import android.os.Parcelable
import android.view.View

class LinearGraphSavedState : View.BaseSavedState {

    var list: List<Point>? = null
        private set

    constructor(list: List<Point>, superState: Parcelable?) : super(superState) {
        this.list = list
    }

    constructor(source: Parcel) : super(source) {
        list?.let { source.readList(it, Point::class.java.classLoader) }
    }

    override fun writeToParcel(out: Parcel?, flags: Int) {
        super.writeToParcel(out, flags)
        out?.writeList(list)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<LinearGraphSavedState> = object : Parcelable.Creator<LinearGraphSavedState> {
            override fun createFromParcel(source: Parcel): LinearGraphSavedState {
                return LinearGraphSavedState(source)
            }

            override fun newArray(size: Int): Array<LinearGraphSavedState> {
                return newArray(size)
            }
        }
    }
}