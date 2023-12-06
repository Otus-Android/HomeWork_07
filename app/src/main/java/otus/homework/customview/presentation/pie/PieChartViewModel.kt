package otus.homework.customview.presentation.pie

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import otus.homework.customview.MyApplication
import otus.homework.customview.domain.Expense
import otus.homework.customview.domain.ExpensesInteractor
import otus.homework.customview.presentation.pie.chart.models.PieData
import otus.homework.customview.presentation.pie.chart.models.PieNode

class PieChartViewModel(
    private val interactor: ExpensesInteractor
) : ViewModel() {

    val uiState get() = _uiState.asStateFlow()
    private val _uiState: MutableStateFlow<PieChartUiState> = MutableStateFlow(PieChartUiState.IDLE)

    fun load(expenses: List<Expense>) {
        val pieData = expenses.map { PieNode(it.amount.toFloat(), it.category) }
        _uiState.value = PieChartUiState.Success(PieData(pieData))
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