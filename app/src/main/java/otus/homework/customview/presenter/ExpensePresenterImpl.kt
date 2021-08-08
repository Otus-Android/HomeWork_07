package otus.homework.customview.presenter

import android.annotation.SuppressLint
import android.widget.TextView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import otus.homework.customview.entity.Expense
import otus.homework.customview.entity.ExpenseItem
import otus.homework.customview.repository.ExpenseRepository
import otus.homework.customview.view.diagram.DiagramView
import otus.homework.customview.view.list.ExpenseListView
import otus.homework.customview.view.list.ItemView
import java.text.SimpleDateFormat
import java.util.*

class ExpensePresenterImpl(
    private val expenseRepository: ExpenseRepository,
    private val diagramView: DiagramView,
    private val totalTextView: TextView,
    private val listExpenseView: ExpenseListView
) : ExpensePresenter {

    private var expense: Expense? = null
    private var _selectCategory: List<ExpenseItem> = listOf()

    override fun attachView() {
        diagramView.setExpensePresenter(this)
        setExpense()
    }

    override fun getCount(): Int =
        _selectCategory.size

    override fun onBind(position: Int, view: ItemView) {
        view.setCategory("Категория: ${_selectCategory[position].category}")
        view.setName("Имя: ${_selectCategory[position].name}")
        view.setAmount("Сумма: ${_selectCategory[position].amount}")
        view.setDate("Дата: ${convertLongToTime(_selectCategory[position].time * 1000)}")
    }


    @SuppressLint("CheckResult", "SetTextI18n")
    private fun setExpense() {
        expenseRepository.getAllExpenses()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                expense = it
                diagramView.showExpenseByCategory(collectExpense(it))
                totalTextView.text = "Всего: ${total(it)} ₽"
                _selectCategory = it
            }, {

            })
    }


    private fun total(expense: Expense): Int {
        return expense.map { it.amount }.sum()
    }

    private fun collectExpense(expense: Expense): List<ExpenseByCategory> {
        val totalExpense = total(expense)
        val expenseByCategory = mutableMapOf<String, ExpenseByCategory>()
        for (item in expense) {
            expenseByCategory[item.category]?.let {
                val newAmount = it.amount + item.amount
                val percent = (newAmount * 100f) / totalExpense
                expenseByCategory[item.category] =
                    ExpenseByCategory(newAmount, it.category, percent)
            } ?: run {
                val percent = (item.amount * 100f) / totalExpense
                expenseByCategory.put(
                    item.category,
                    ExpenseByCategory(item.amount, item.category, percent)
                )
            }
        }
        return expenseByCategory.values.toList()
    }

    override fun onClickCategory(category: String) {
        _selectCategory = expense?.filter { it.category == category }?.toList() ?: listOf()
        listExpenseView.notifyAllDataChange()
    }

    override fun onClickAll() {
        expense?.let {
            _selectCategory = it.toList()
            listExpenseView.notifyAllDataChange()
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun convertLongToTime(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("dd/MM/yy HH:mm")
        return format.format(date)
    }

}