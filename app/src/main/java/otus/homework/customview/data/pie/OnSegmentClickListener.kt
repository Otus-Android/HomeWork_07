package otus.homework.customview.data.pie

interface OnSegmentClickListener {
  fun action(category: String, amount: Int)
}