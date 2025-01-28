package otus.homework.customview.data

data class GraphBarModel(
    val category: String,
    val color: Int,
    val listCategory: MutableList<CategoryForList>
)

data class CategoryForList(
    val day: Int,
    val sum: Int
)
