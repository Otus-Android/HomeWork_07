package otus.homework.customview.data.graph

class GraphData {
  val items = mutableListOf<CategoryDetail>()

  fun appendItem(withTitle: String, andSum: Int, atTime: Long) {
    items.add(CategoryDetail(withTitle, andSum, atTime))
  }
}
