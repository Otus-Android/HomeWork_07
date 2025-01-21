package otus.homework.customview

interface PieChartInterface {
    fun setOnCategoryClickListener(onCategoryClickListener: PieChartView.OnCategoryClickListener)
    fun setData(expensesInfoList: List<ExpensesInfoItem>)
}