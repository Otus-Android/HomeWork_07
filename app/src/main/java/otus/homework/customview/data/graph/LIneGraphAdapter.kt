package otus.homework.customview.data.graph

import otus.homework.customview.util.GraphDataSet


class LineGraphAdapter(
  private var yData: GraphDataSet = arrayListOf()
) : ChartAdapter() {
  override val count: Int
    get() = yData.size

  override fun getItem(index: Int): Any = yData[index]

  override fun getX(index: Int): Float = yData[index].first

  override fun getY(index: Int): Float = yData[index].second

  override fun hasBaseline(): Boolean = containsNegativeValue()

  fun setData(data: GraphDataSet) {
    this.yData = data
    notifyDataSetChanged()
  }

  fun hideGraph() {
    var minValue = Float.MIN_VALUE
    for (data in yData) {
      if (data.second < minValue) {
        minValue = data.second
      }
    }

    var i = 0
    while (i < yData.size) {
      yData[i] = Pair(yData[i].first, minValue)
      i++
    }
    notifyDataSetChanged()
  }

  fun clearData() {
    setData(arrayListOf())
  }

  private fun containsNegativeValue(): Boolean {
    for (value in yData) {
      if (value.second < 0) return true
    }
    return false
  }
}