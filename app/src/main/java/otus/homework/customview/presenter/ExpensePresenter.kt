package otus.homework.customview.presenter

import otus.homework.customview.view.list.ItemView

interface ExpensePresenter {
    fun attachView()
    fun getCount():Int
    fun onBind(position:Int, view:ItemView)
    fun onClickCategory(category: String)
    fun onClickAll()
}