package otus.homework.customview.pie_chart

import android.os.Parcel
import android.os.Parcelable
import android.view.View

internal class SavedState : View.BaseSavedState {
    var items: List<Category>? = null

    constructor(superState: Parcelable) : super(superState)

    constructor(source: Parcel) : super(source) {
        items?.let { source.readList(it, Category::class.java.classLoader) }
    }

    override fun writeToParcel(out: Parcel, flags: Int) {
        super.writeToParcel(out, flags)
        out.writeList(items)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
            override fun createFromParcel(source: Parcel): SavedState {
                return SavedState(source)
            }

            override fun newArray(size: Int): Array<SavedState> {
                return newArray(size)
            }
        }
    }
}