package otus.homework.customview.presentation.pie.chart.models

import android.graphics.Color
import kotlin.random.Random

class PieDataProvider {

    private val random = Random

    private var pieAngleNodes = emptyList<InnerPieAngleNode>()

    fun calculate(pieData: PieData<Float>) {
        val generalAmount = pieData.nodes.sumOf { it.value.toDouble() }

        var count = 0f
        val newValues2 = pieData.nodes.map { (value, label) ->
            InnerPieAngleNode(
                label = label.orEmpty(),
                angleStart = count,
                angleSweep = value.toFloat() / (generalAmount.toFloat()) * 360f,
                color = Color.argb(
                    255,
                    random.nextInt(256),
                    random.nextInt(256),
                    random.nextInt(256)
                )
            ).also { angleNode ->
                count += angleNode.angleSweep
            }
        }

        pieAngleNodes = newValues2.sortedBy { it.angleStart }
    }


    fun getNodes(): List<InnerPieAngleNode> = pieAngleNodes

    fun getCategory(angle: Float) =
        pieAngleNodes.find { it.angleStart <= angle && it.angleStart + it.angleSweep > angle }?.label


    fun getNode(angle: Float) =
        pieAngleNodes.find { it.angleStart <= angle && it.angleStart + it.angleSweep > angle }
}