package otus.homework.customview.recycler_view

import androidx.recyclerview.widget.DiffUtil
import otus.homework.customview.entities.Spending

class SpendingItemDiffCallback : DiffUtil.ItemCallback<Spending>() {

    override fun areItemsTheSame(old: Spending, aNew: Spending): Boolean {
        return old.name == aNew.name
    }

    override fun areContentsTheSame(old: Spending, aNew: Spending): Boolean {
        return old == aNew
    }

}