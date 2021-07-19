package otus.homework.customview.pie_chart

import android.os.Parcel
import android.os.Parcelable
import android.view.View

internal class CategorySavedState : View.BaseSavedState {
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
        val CREATOR: Parcelable.Creator<CategorySavedState> = object : Parcelable.Creator<CategorySavedState> {
            override fun createFromParcel(source: Parcel): CategorySavedState {
                return CategorySavedState(source)
            }

            override fun newArray(size: Int): Array<CategorySavedState> {
                return newArray(size)
            }
        }
    }
}