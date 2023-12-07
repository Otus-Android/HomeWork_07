package otus.homework.customview.presentation.pie

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import otus.homework.customview.MyApplication
import otus.homework.customview.domain.Expense
import otus.homework.customview.domain.ExpensesInteractor
import otus.homework.customview.presentation.pie.chart.PieStyle
import otus.homework.customview.presentation.pie.converters.PieDataConverter

class PieChartViewModel(
    private val interactor: ExpensesInteractor,
    private val converter: PieDataConverter = PieDataConverter()
) : ViewModel() {

    val uiState get() = _uiState.asStateFlow()
    private val _uiState = MutableStateFlow(PieChartUiState())

    fun load(expenses: List<Expense>) {
        val pieData = converter.convert(expenses)
        _uiState.update { it.copy(data = pieData) }
    }

    fun onStyleButtonClick() {
        _uiState.update {
            val style = if (it.style == PieStyle.PIE) PieStyle.DONUT else PieStyle.PIE
            it.copy(style = style)
        }
    }

    companion object {

        val Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application =
                    checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                val diContainer = MyApplication.diContainer(application)
                return PieChartViewModel(interactor = diContainer.interactor) as T
            }
        }
    }
}