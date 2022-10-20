package otus.homework.customview.recycler_view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import otus.homework.customview.databinding.ItemCategoryBinding
import otus.homework.customview.entities.Category

class CategoryListAdapter :
    ListAdapter<Category, CategoryItemViewHolder>(CategoryItemDiffCallback()) {

    var onListClickListener: ((Category) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryItemViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryItemViewHolder(
            binding,
            onListClickListener
        )
    }

    override fun onBindViewHolder(viewHolder: CategoryItemViewHolder, position: Int) {
        val item = getItem(position)
        viewHolder.bindItem(item)
    }
}