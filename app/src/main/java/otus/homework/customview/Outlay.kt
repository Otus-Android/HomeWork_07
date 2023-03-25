package otus.homework.customview

data class Outlay(
    private val id: Int,
    private val name: String,
    private val amount: Int,
    private val category: String,
    private val time: Long
)