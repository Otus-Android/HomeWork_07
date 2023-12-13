package otus.homework.customview.presentation.pie.chart.storages

import otus.homework.customview.presentation.pie.chart.PieData
import otus.homework.customview.presentation.pie.chart.converters.PieAresNodeConverter
import otus.homework.customview.presentation.pie.chart.models.PieAreaNode

/**
 * Хранилище внутренних моделей узлов кругового графика
 *
 * @param converter конвертер внутренних моделей узлов кругового графика
 */
internal class PieDataStorage(private val converter: PieAresNodeConverter = PieAresNodeConverter()) {

    var origin = PieData()
    private val pieAngleNodes = mutableListOf<PieAreaNode>()

    /** Обновить список внутренних моделей узлов на основании данных [PieData] */
    fun update(pieData: PieData) {
        origin = pieData
        val areaNodes = converter.convert(pieData.nodes)
        pieAngleNodes.clear()
        pieAngleNodes.addAll(areaNodes)
    }

    /** Получить список внутренних моделей узлов кругового графика */
    fun getNodes(): List<PieAreaNode> = pieAngleNodes

    /** Получить внутреннюю модель узла по углу круговой диаграммы */
    fun getNodeByAngle(angle: Float) =
        pieAngleNodes.find { it.startAngle <= angle && it.startAngle + it.sweepAngle > angle }
}