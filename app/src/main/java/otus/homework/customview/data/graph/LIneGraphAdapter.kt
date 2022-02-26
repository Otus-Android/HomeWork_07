package otus.homework.customview.data.graph

import otus.homework.customview.util.GraphDataSet


class LineGraphAdapter(
  private var lineGraphData: GraphDataSet = arrayListOf()
) : ChartAdapter() {
  override val count: Int
    get() = lineGraphData.size

  override fun getItem(index: Int): Any = lineGraphData[index]

  override fun getX(index: Int): Float = lineGraphData[index].first

  override fun getY(index: Int): Float = lineGraphData[index].second

  override fun hasBaseline(): Boolean = containsNegativeValue()

  fun setData(data: GraphDataSet) {
    this.lineGraphData = data
    notifyDataSetChanged()
  }

  fun hideGraph() {
    var minValue = Float.MIN_VALUE
    for (data in lineGraphData) {
      if (data.second < minValue) {
        minValue = data.second
      }
    }

    var i = 0
    while (i < lineGraphData.size) {
      lineGraphData[i] = Pair(lineGraphData[i].first, minValue)
      i++
    }
    notifyDataSetChanged()
  }

  fun clearData() {
    setData(arrayListOf())
  }

  private fun containsNegativeValue(): Boolean {
    for (value in lineGraphData) {
      if (value.second < 0) return true
    }
    return false
  }
}