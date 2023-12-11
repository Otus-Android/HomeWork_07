package otus.homework.customview.presentation.line.chart.storages

import otus.homework.customview.presentation.line.chart.LineData
import otus.homework.customview.presentation.line.chart.converters.LineAreaNodeConverter
import otus.homework.customview.presentation.line.chart.models.LineAreaNode

/**
 * Хранилище внутренних моделей узлов линейного графика
 *
 * @param areaStorage хранилище параметров областей `View`
 * @param converter конвертер внутренних моделей узлов линейного графика
 */
internal class LineDataStorage(
    private val areaStorage: LineAreaStorage,
    private val converter: LineAreaNodeConverter = LineAreaNodeConverter()
) {

    private var origin = LineData()
    private val nodes = mutableListOf<LineAreaNode>()

    /** Обновить список внутренних моделей узлов на основании данных [LineData] */
    fun update(data: LineData) {
        origin = data

        val areaNodes = converter.convert(data.nodes, areaStorage.chart)

        nodes.clear()
        nodes.addAll(areaNodes)
    }

    /** Выполнить повторное обновление  */
    fun reupdate() = update(origin)

    /** Получить список внутренних моделей узлов линейного графика */
    fun getNodes(): List<LineAreaNode> = nodes

    /** Получить внутреннюю модель узла по координате на оси X */
    fun getNodeByX(x: Float) = nodes.findLast { it.x < x }

}
