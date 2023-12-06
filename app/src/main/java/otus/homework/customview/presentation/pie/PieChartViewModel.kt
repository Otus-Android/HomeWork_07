package otus.homework.customview.presentation.pie

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

class PieChartViewModel(
    private val interactor: ExpensesInteractor
) : ViewModel() {

    val uiState get() = _uiState.asStateFlow()
    private val _uiState: MutableStateFlow<PieChartUiState> = MutableStateFlow(PieChartUiState.IDLE)

    private val exceptionHandler = CoroutineExceptionHandler { _, e ->
        _uiState.value = PieChartUiState.Error(e.toString())
    }

    fun loadExpenses() {
        viewModelScope.launch(exceptionHandler) {
            _uiState.value = PieChartUiState.Loading
            try {
                val expenses = interactor.getExpenses()
                _uiState.value = PieChartUiState.Success(expenses)
            } catch (e: ExpensesException) {
                _uiState.value = PieChartUiState.Error(e.toString())
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
                return PieChartViewModel(interactor = diContainer.interactor) as T
            }
        }
    }
}