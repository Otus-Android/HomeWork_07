package otus.homework.customview.view.list

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView

class ExpenseListViewImpl @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr), ExpenseListView {


    @SuppressLint("NotifyDataSetChanged")
    override fun notifyAllDataChange() {
        adapter?.notifyDataSetChanged()
    }
}

interface ExpenseListView {

    fun notifyAllDataChange()


}