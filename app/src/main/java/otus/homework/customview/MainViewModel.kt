package otus.homework.customview

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import otus.homework.customview.chart.ChartData
import java.io.BufferedReader

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _currentColor = MutableStateFlow<Int?>(null)
    val currentColor: StateFlow<Int?> = _currentColor.asStateFlow()

    private val _labelsData = MutableStateFlow(LabelsData())
    val labelsData: StateFlow<LabelsData> = _labelsData.asStateFlow()

    private var sourceData: List<ChartData> = emptyList()

    private val _pieData = MutableStateFlow<List<ChartData>>(emptyList())
    val pieData: StateFlow<List<ChartData>> = _pieData.asStateFlow()

    private val _lineData = MutableStateFlow<List<ChartData>>(emptyList())
    val lineData: StateFlow<List<ChartData>> = _lineData.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            // исходные данные из json-файла
            sourceData = application.resources.openRawResource(R.raw.payload).use {
                it.bufferedReader().use(BufferedReader::readText)
            }.let {
                Json.decodeFromString(it)
            }
            // собрать данные для pie-чарта
            _pieData.value = sourceData.groupBy {
                it.category
            }.map { mapEntry ->
                ChartData(
                    id = mapEntry.value.first().id,
                    name = "",
                    amount = mapEntry.value.sumOf { it.amount },
                    category = mapEntry.key,
                    time = 0L
                )
            }
        }
    }


    /**
     * Отрабатывает выбор детализации трат.
     */
    fun changeSelected(id: Int?, color: Int?) {
        // отработать цвет
        _currentColor.value = color
        // получить данные, которые выбраны
        val data = id?.let {
            pieData.value.find {
                it.id == id
            }
        }
        // обновить лейблы
        _labelsData.value = if (data == null) LabelsData() else LabelsData(
            title = data.category,
            sum = "${data.amount} из ${sourceData.sumOf { it.amount }}"
        )
        // обновить линейный чарт
        _lineData.value = data?.let { selectedData ->
            sourceData.filter {
                it.category == selectedData.category
            }.sortedBy {
                it.time
            }
        } ?: emptyList()
    }

    data class LabelsData(
        val title: String = "Tap",
        val sum: String = "on pie"
    )

}