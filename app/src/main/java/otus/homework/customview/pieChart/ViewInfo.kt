package otus.homework.customview.pieChart

import kotlin.math.min

class ViewInfo {

    var width: Int = 0
        set(value) {
            field = value
            calculateAllViewParams()
        }
    var height: Int = 0
        set(value) {
            field = value
            calculateAllViewParams()
        }

    // центр View
    private var cX = 0
    private var cY = 0

    // размер родителя
    private var parentViewSize = 0f
    private var halfViewSize = 0f

    private fun calculateAllViewParams() {
        if (width > 0 && height > 0) {

            cX = width / 2
            cY = height / 2

            parentViewSize = min(width, height).toFloat()
            halfViewSize = parentViewSize / 2

        }
    }

    fun getViewSize() = parentViewSize
    fun getHalfViewSize() = parentViewSize / 2

    fun getCenterX() = cX
    fun getCenterY() = cY
}