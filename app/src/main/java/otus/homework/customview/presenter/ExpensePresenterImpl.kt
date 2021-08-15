package otus.homework.customview.presenter

import android.annotation.SuppressLint
import android.os.Build
import android.widget.TextView
import androidx.annotation.RequiresApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import otus.homework.customview.entity.Expense
import otus.homework.customview.entity.ExpenseItem
import otus.homework.customview.repository.ExpenseRepository
import otus.homework.customview.view.diagram.DiagramView
import otus.homework.customview.view.graph.GraphView
import otus.homework.customview.view.list.ExpenseListView
import otus.homework.customview.view.list.ItemView
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

class ExpensePresenterImpl(
    private val expenseRepository: ExpenseRepository,
    private val diagramView: DiagramView,
    private val totalTextView: TextView,
    private val listExpenseView: ExpenseListView,
    private val graphView: GraphView
) : ExpensePresenter {

    private var expense: Expense? = null
    private var _selectCategory: List<ExpenseItem> = listOf()
    private val compositeDisposable = CompositeDisposable()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun attachView() {
        diagramView.setExpensePresenter(this)
        setExpense()
        graphView.goneView()
    }

    override fun getCount(): Int =
        _selectCategory.size

    override fun onBind(position: Int, view: ItemView) {
        view.setCategory("Категория: ${_selectCategory[position].category}")
        view.setName("Где: ${_selectCategory[position].name}")
        view.setAmount("Сумма: ${_selectCategory[position].amount}")
        view.setDate("Дата: ${longToDateTime(_selectCategory[position].time * 1000)}")
    }


    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("CheckResult", "SetTextI18n")
    private fun setExpense() {
        compositeDisposable.add(
            expenseRepository.getAllExpenses()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    expense = it
                    diagramView.showExpenseByCategory(collectExpense(it))
                    totalTextView.text = "Всего: ${total(it)} ₽"
                    _selectCategory = it
                    graphView.goneView()
                }, {

                })
        )
    }


    private fun total(expense: Expense): Int {
        return expense.map { it.amount }.sum()
    }

    private fun maxAmount(expenseItemList: List<ExpenseItem>): Int {
        return expenseItemList.map { it.amount }.maxOrNull() ?: 0
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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClickCategory(category: String) {
        _selectCategory = expense?.filter { it.category == category }?.toList() ?: listOf()
        graphView.showExpense(collectExpenseByDay(), maxAmount(_selectCategory))
        graphView.showView()
        listExpenseView.notifyAllDataChange()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClickAll() {
        expense?.let {
            _selectCategory = it.toList()
            graphView.goneView()
            listExpenseView.notifyAllDataChange()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun collectExpenseByDay(): Map<LocalDate, ExpenseByDay> {

        val result = mutableMapOf<LocalDate, ExpenseByDay>()

        for (item in _selectCategory) {
            result[longToDate(item.time * 1000)]?.let {
                val sumAmount = item.amount + it.amount
                result.put(longToDate(item.time * 1000), it.copy(amount = sumAmount))
            } ?: run {
                result.put(
                    longToDate(item.time * 1000),
                    ExpenseByDay(item.amount, longToDate(item.time), item.category)
                )
            }
        }
        return result
    }

    override fun onClear() {
        compositeDisposable.clear()
    }

    @SuppressLint("SimpleDateFormat")
    private fun longToDateTime(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("dd/MM/yy HH:mm")
        return format.format(date)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SimpleDateFormat")
    private fun longToDate(time: Long): LocalDate {
        val date = Date(time)
        val format = SimpleDateFormat("yyyy-MM-dd")
        return LocalDate.parse(format.format(date))
    }

}