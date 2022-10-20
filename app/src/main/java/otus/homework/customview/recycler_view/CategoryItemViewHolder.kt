package otus.homework.customview.recycler_view

import androidx.recyclerview.widget.RecyclerView
import otus.homework.customview.R
import otus.homework.customview.databinding.ItemCategoryBinding
import otus.homework.customview.entities.Category

class CategoryItemViewHolder(
    private val binding: ItemCategoryBinding,
    private val onListClickListener: ((Category) -> Unit)?
) : RecyclerView.ViewHolder(binding.root) {


    fun bindItem(category: Category) {
        with(binding) {
            tvName.text = category.name
            tvTotal.text = root.resources.getString(R.string.roubles, category.total)
            tvColor.setBackgroundColor(category.color)
            root.setOnClickListener {
                onListClickListener?.invoke(category)
            }
        }
    }
}