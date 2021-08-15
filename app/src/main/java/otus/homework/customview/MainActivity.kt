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
import otus.homework.customview.view.graph.GraphView
import otus.homework.customview.view.list.ExpenseListAdapter
import otus.homework.customview.view.list.ExpenseListViewImpl

class MainActivity : AppCompatActivity() {

    private lateinit var expensePresenter: ExpensePresenter

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val expenseListView: ExpenseListViewImpl = findViewById(R.id.expense_rw)
        val repository: ExpenseRepository = ExpenseRepositoryImpl(applicationContext)
        val diagramView: DiagramView = findViewById(R.id.diagram_custom_view)
        val totalTextView: TextView = findViewById(R.id.total_text_View)
        val graphView:GraphView = findViewById(R.id.graph_custom_view)

        expensePresenter =
            ExpensePresenterImpl(repository, diagramView, totalTextView, expenseListView, graphView)

        expenseListView.layoutManager = LinearLayoutManager(this)
        expenseListView.adapter = ExpenseListAdapter(expensePresenter)
        expensePresenter.attachView()

        val dividerItemDecoration = DividerItemDecoration(this, RecyclerView.VERTICAL)
        dividerItemDecoration.setDrawable(resources.getDrawable(R.drawable.divider_drawable))
        expenseListView.addItemDecoration(dividerItemDecoration)
    }

    override fun onDestroy() {
        super.onDestroy()
        expensePresenter.onClear()

    }
}