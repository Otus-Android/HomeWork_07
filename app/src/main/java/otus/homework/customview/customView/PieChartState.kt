package otus.homework.customview.customView;


import android.os.Parcel
import android.os.Parcelable
import android.view.View
import otus.homework.customview.models.Category

class PieChartState(superState: Parcelable?) : View.BaseSavedState(superState) {

    var categories = mapOf<String, Category>()
    var sumExpense = 0
    var categoryToSum = mapOf<String, Int>()
    var hasTapped = false
    var tapedAngle = 0.0
    var colors = mapOf<String, Int>()

    override fun writeToParcel(out: Parcel, flags: Int) {
        super.writeToParcel(out, flags)
        out.writeMap(categories)
    }
}