package otus.homework.customview.presentation.pie

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import otus.homework.customview.domain.models.Category
import otus.homework.customview.presentation.pie.chart.PieStyle
import otus.homework.customview.presentation.pie.converters.PieDataConverter

/**
 * `ViewModel` кругового графика данных по категориям расходов
 *
 * @param converter конвертер данных кругового графика
 */
class PieChartViewModel(
    private val converter: PieDataConverter = PieDataConverter()
) : ViewModel() {

    /** Состояние отображения кругового графика */
    val uiState get() = _uiState.asStateFlow()
    private val _uiState = MutableStateFlow(PieChartUiState())

    /** Обработать данные по категориям расходов */
    fun process(categories: List<Category>) {
        val pieData = converter.convert(categories)
        _uiState.update { it.copy(data = pieData) }
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
     * Обработать нажатие на кнопку смены стиля графика
     *
     * @param isChecked признак стиля графика в виде "бублика"
     */
    fun onStyleChanged(isChecked: Boolean) {
        _uiState.update {
            val style = if (isChecked) PieStyle.DONUT else PieStyle.PIE
            it.copy(style = style)
        }
    }

    companion object {

        /** Фабрика создания `ViewModel` */
        val Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PieChartViewModel() as T
            }
        }
    }
}