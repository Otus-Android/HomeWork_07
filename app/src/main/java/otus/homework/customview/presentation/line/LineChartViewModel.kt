package otus.homework.customview.presentation.line

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import otus.homework.customview.domain.models.Category
import otus.homework.customview.presentation.line.converters.LineDataConverter

/**
 * `ViewModel` линейного графика данных по категориям расходов
 *
 * @param converter конвертер данных линейного графика
 */
class LineChartViewModel(
    private val converter: LineDataConverter = LineDataConverter()
) : ViewModel() {

    /** Состояние отображения линейного графика */
    val uiState get() = _uiState.asStateFlow()
    private val _uiState: MutableStateFlow<LineChartUiState> = MutableStateFlow(LineChartUiState())

    /** Обработать данные по категориям расходов */
    fun process(categories: List<Category>) {
        val category = categories.firstOrNull()
        val lineData = category?.let { converter.convert(it) }
        _uiState.update { it.copy(lineData = lineData, categories = categories) }
    }

    /**
     * Обработать нажатие на кнопку отображения отладочной информации
     *
     * @param isChecked признак доступности отладочной информации
     */
    fun onDebugChanged(isChecked: Boolean) {
        _uiState.update { it.copy(isDebugEnabled = isChecked) }
    }

    /**
     * Обработать выбор текущей категории
     *
     * @param position позиция выбранной категории
     */
    fun onCategorySelected(position: Int) {
        _uiState.update { state ->
            val lineData = state.categories.getOrNull(position)?.let { converter.convert(it) }
            state.copy(lineData = lineData)
        }
    }

    companion object {

        /** Фабрика создания `ViewModel` */
        val Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return LineChartViewModel() as T
            }
        }
    }
}