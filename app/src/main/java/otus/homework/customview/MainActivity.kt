package otus.homework.customview

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import otus.homework.customview.presenter.ExpensePresenter
import otus.homework.customview.presenter.ExpensePresenterImpl
import otus.homework.customview.repository.ExpenseRepository
import otus.homework.customview.repository.ExpenseRepositoryImpl
import otus.homework.customview.view.diagram.DiagramView
import otus.homework.customview.view.list.ExpenseListAdapter
import otus.homework.customview.view.list.ExpenseListViewImpl

class MainActivity : AppCompatActivity() {


    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val expenseListView: ExpenseListViewImpl = findViewById(R.id.expense_rw)
        val repository: ExpenseRepository = ExpenseRepositoryImpl(applicationContext)
        val diagramView: DiagramView = findViewById(R.id.custom_view)
        val totalTextView: TextView = findViewById(R.id.total_text_View)

        val expensePresenter: ExpensePresenter =
            ExpensePresenterImpl(repository, diagramView, totalTextView, expenseListView)

        expenseListView.layoutManager = LinearLayoutManager(this)
        expenseListView.adapter = ExpenseListAdapter(expensePresenter)
        expensePresenter.attachView()

        val dividerItemDecoration = DividerItemDecoration(this, RecyclerView.VERTICAL)
        dividerItemDecoration.setDrawable(resources.getDrawable(R.drawable.divider_drawable))
        expenseListView.addItemDecoration(dividerItemDecoration)
    }
}