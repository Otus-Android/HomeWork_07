package otus.homework.customview.piechart

import java.io.Serializable
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class PieChartData() : Serializable {
    val pieces: MutableList<PieceModel> = mutableListOf()

    fun addPiece(
        id: String,
        name: String,
        category: String,
        color: Int,
        amount: String,
        currentAngle: Float
    ) {
        pieces.add(PieceModel(id, name, category, color, amount, currentAngle))
    }

    fun getXByAngle(angle: Float, currentSize: Int): Float {
        return cos(Math.toRadians(angle.toDouble())).toFloat() * currentSize * .45f + currentSize / 2f
    }

    fun getYByAngle(angle: Float, currentSize: Int): Float {
        return sin(Math.toRadians(angle.toDouble())).toFloat() * currentSize * .45f + currentSize / 2f
    }

    fun getAngleByXY(x: Float, y: Float): Float {
        val angle = Math.toDegrees(atan2(y, x).toDouble()).toFloat()
        return if (angle < 0) angle + 360 else angle
    }
}