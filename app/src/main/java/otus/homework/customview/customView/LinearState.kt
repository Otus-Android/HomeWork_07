package otus.homework.customview.customView;


import android.os.Parcel
import android.os.Parcelable
import android.view.View
import otus.homework.customview.models.Category
import java.util.Date
import java.util.TreeSet

class LinearState(superState: Parcelable?) : View.BaseSavedState(superState) {

    var categories = mapOf<String, Category>()
    var sumExpense = 0
    var categoryToSum = mapOf<String, Int>()
    var colors = mapOf<String, Int>()
    var expensesByDays = mapOf<String, List<Int>>()
    var uniqueDays = TreeSet<Date>()
    var columnsCount = 0

    override fun writeToParcel(out: Parcel, flags: Int) {
        super.writeToParcel(out, flags)
        out.writeMap(categories)
    }
}