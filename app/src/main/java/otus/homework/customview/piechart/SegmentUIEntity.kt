package otus.homework.customview.piechart

data class SegmentsDataEntity(
    val month: String,
    val data: List<SegmentUIEntity>
)

data class SegmentUIEntity(
    val id: Int,
    val name: String,
    val amount: Int,
    val category: String,
    val time: Long
)
