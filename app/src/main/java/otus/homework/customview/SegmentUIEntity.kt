package otus.homework.customview

data class SegmentsDataEntity(
    val data: List<SegmentUIEntity>
)

data class SegmentUIEntity(
    val id: Int,
    val name: String,
    val amount: Int,
    val category: String,
    val time: Long
)
