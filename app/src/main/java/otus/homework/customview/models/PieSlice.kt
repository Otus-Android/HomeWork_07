package otus.homework.customview.models

data class PieSlice(
    var weight: Float,
    val color: Int,
    var fromAngle: Float? = null,
    var toAngle: Float? = null,
    val totalAmount: Int
) {
    fun isIn(angle: Double): Boolean {
        return if (fromAngle != null && toAngle != null) {
            angle in fromAngle!!..(fromAngle!! + toAngle!!)
        } else {
            false
        }
    }
}