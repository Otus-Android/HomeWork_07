package otus.homework.customview.recycler_view

import androidx.recyclerview.widget.RecyclerView
import otus.homework.customview.R
import otus.homework.customview.databinding.ItemSpendingBinding
import otus.homework.customview.entities.Spending
import otus.homework.customview.entities.Time

class SpendingItemViewHolder(
    private val binding: ItemSpendingBinding,
    private val onListClickListener: ((Spending) -> Unit)?
) : RecyclerView.ViewHolder(binding.root) {

    private val time = Time()

    fun bindItem(spending: Spending) {
        with(binding) {
            tvName.text = spending.name
            tvDate.text = time.timeToString(spending.time)
            tvTotal.text = root.resources.getString(R.string.roubles, spending.amount)
            root.setOnClickListener {
                onListClickListener?.invoke(spending)
            }
        }
    }
}