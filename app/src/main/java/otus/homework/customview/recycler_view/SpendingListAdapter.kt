package otus.homework.customview.recycler_view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import otus.homework.customview.databinding.ItemSpendingBinding
import otus.homework.customview.entities.Spending

class SpendingListAdapter :
    ListAdapter<Spending, SpendingItemViewHolder>(SpendingItemDiffCallback()) {

    var onListClickListener: ((Spending) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpendingItemViewHolder {
        val binding = ItemSpendingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SpendingItemViewHolder(
            binding,
            onListClickListener
        )
    }

    override fun onBindViewHolder(viewHolder: SpendingItemViewHolder, position: Int) {
        val item = getItem(position)
        viewHolder.bindItem(item)
    }
}