package otus.homework.customview.model

import android.os.Parcel
import android.os.Parcelable
import android.view.View.BaseSavedState

class ExpensesByCategoryState : BaseSavedState {

    var itemsList: List<ExpensesByCategory> = listOf()

    constructor(superState: Parcelable?) : super(superState)

    private constructor(parcel: Parcel) : super(parcel) {
        itemsList = listOf()
        parcel.readTypedList(itemsList, ExpensesByCategory.CREATOR)
    }

    override fun writeToParcel(out: Parcel, flags: Int) {
        super.writeToParcel(out, flags)
        out.writeList(itemsList)
    }
}