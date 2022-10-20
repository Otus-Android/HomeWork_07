package otus.homework.customview.recycler_view

import androidx.recyclerview.widget.DiffUtil
import otus.homework.customview.entities.Category

class CategoryItemDiffCallback : DiffUtil.ItemCallback<Category>() {

    override fun areItemsTheSame(old: Category, aNew: Category): Boolean {
        return old.name == aNew.name
    }

    override fun areContentsTheSame(old: Category, aNew: Category): Boolean {
        return old == aNew
    }

}