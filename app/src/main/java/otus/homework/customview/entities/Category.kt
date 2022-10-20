package otus.homework.customview.entities

data class Category(
    var id: Int = UNDEFINED_ID,
    val name: String,
    var total: Int = 0,
    var color: Int = 0
) {

    companion object {
        const val UNDEFINED_ID = -1
    }
}
