package otus.homework.customview.view.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import otus.homework.customview.R
import otus.homework.customview.presenter.ExpensePresenter

class ExpenseListAdapter(
    private val expensePresenter: ExpensePresenter
) : RecyclerView.Adapter<ExpenseListAdapter.ExpenseListHolder>() {

    class ExpenseListHolder(itemView: View) : RecyclerView.ViewHolder(itemView), ItemView
       {
           override fun setCategory(category: String) {
               itemView.findViewById<TextView>(R.id.category_tv).text = category
           }
           override fun setName(name: String) {
               itemView.findViewById<TextView>(R.id.name_tv).text = name
           }
           override fun setAmount(amount: String) {
               itemView.findViewById<TextView>(R.id.amount_tv).text = amount
           }
           override fun setDate(date: String) {
               itemView.findViewById<TextView>(R.id.date_tv).text = date
           }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseListHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_layout, parent, false)
        return ExpenseListHolder(itemView)
    }

    override fun onBindViewHolder(holder: ExpenseListHolder, position: Int) {
        expensePresenter.onBind(position, holder)
    }

    override fun getItemCount(): Int = expensePresenter.getCount()
}