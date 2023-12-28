package otus.homework.customview.pojo

data class GraphsBuildDetailsData(
    val category: String,
    val color: Int,
    val rageDate: Pair<Long, Long>,
    val rageAmount: Pair<Int, Int>,
    val detailsCategory: List<Details>
)
