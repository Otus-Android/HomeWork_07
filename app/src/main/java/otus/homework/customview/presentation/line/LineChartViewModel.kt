package otus.homework.customview.presentation.line

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import otus.homework.customview.MyApplication
import otus.homework.customview.data.ExpensesException
import otus.homework.customview.domain.ExpensesInteractor

class LineChartViewModel(
    private val interactor: ExpensesInteractor
) : ViewModel() {

    val uiState get() = _uiState.asStateFlow()
    private val _uiState: MutableStateFlow<LineChartUiState> = MutableStateFlow(LineChartUiState.IDLE)

    private val exceptionHandler = CoroutineExceptionHandler { _, e ->
        _uiState.value = LineChartUiState.Error(e.toString())
    }

    fun loadExpenses() {
        viewModelScope.launch(exceptionHandler) {
            _uiState.value = LineChartUiState.Loading
            try {
                val expenses = interactor.getExpenses()
                _uiState.value = LineChartUiState.Success(expenses)
            } catch (e: ExpensesException) {
                _uiState.value = LineChartUiState.Error(e.toString())
            }
        }
    }

    companion object {

        val Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application =
                    checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                val diContainer = MyApplication.diContainer(application)
                return LineChartViewModel(interactor = diContainer.interactor) as T
            }
        }
    }
}