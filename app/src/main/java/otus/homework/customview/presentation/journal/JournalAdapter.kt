package otus.homework.customview.presentation.journal

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import otus.homework.customview.R
import otus.homework.customview.databinding.ItemJournalBinding
import otus.homework.customview.domain.models.Expense

/**
 * Адаптер журнала данных по расходам
 */
class JournalAdapter : ListAdapter<Expense, JournalAdapter.JournalViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JournalViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemJournalBinding.inflate(inflater, parent, false)
        return JournalViewHolder(binding)
    }

    override fun onBindViewHolder(holder: JournalViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * `ViewHolder` журнала данных по расходам
     *
     * @param binding данные отображения
     */
    class JournalViewHolder(private val binding: ItemJournalBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * Осуществить привязку данных [Expense]
         */
        fun bind(expense: Expense) {
            val resources = itemView.context.resources
            with(binding) {
                idTextView.text = resources.getString(R.string.journal_item_id, expense.id)
                nameTextView.text = resources.getString(R.string.journal_item_name, expense.name)
                amountTextView.text =
                    resources.getString(R.string.journal_item_amount, expense.amount)
                categoryTextView.text =
                    resources.getString(R.string.journal_item_category, expense.category)
                dateTextView.text = resources.getString(R.string.journal_item_time, expense.time)
            }
        }
    }

    /**
     * `DiffCallback` данных по расходам
     */
    private class DiffCallback : DiffUtil.ItemCallback<Expense>() {

        override fun areItemsTheSame(oldItem: Expense, newItem: Expense) = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Expense, newItem: Expense) = oldItem == newItem
    }
}