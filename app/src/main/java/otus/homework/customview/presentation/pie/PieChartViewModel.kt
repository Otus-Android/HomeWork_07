package otus.homework.customview.presentation.pie

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import otus.homework.customview.domain.models.Expense
import otus.homework.customview.presentation.pie.chart.PieStyle
import otus.homework.customview.presentation.pie.converters.PieDataConverter

class PieChartViewModel(
    private val converter: PieDataConverter = PieDataConverter()
) : ViewModel() {

    val uiState get() = _uiState.asStateFlow()
    private val _uiState = MutableStateFlow(PieChartUiState())

    fun load(expenses: List<Expense>) {
        val pieData = converter.convert(expenses)
        _uiState.update { it.copy(data = pieData) }
    }

    fun onStyleChanged(isChecked: Boolean) {
        _uiState.update {
            val style = if (isChecked) PieStyle.DONUT else PieStyle.PIE
            it.copy(style = style)
        }
    }

    companion object {

        val Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PieChartViewModel() as T
            }
        }
    }
}